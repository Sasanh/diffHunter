/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffhunter;

import com.sleepycat.je.Database;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author hashemis
 */
public class DiffHunter
{

    /**
     * @param args the command line arguments
     * @throws org.apache.commons.cli.ParseException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws ParseException, IOException
    {

        //String test_ = Paths.get("J:\\VishalData\\additional\\", "Sasan" + "_BDB").toAbsolutePath().toString();

        // TODO code application logic here
        /*args = new String[]
        {
            "-i", "-b", "J:\\VishalData\\additional\\Ptbp2_E18_5_cortex_CLIP_mm9_plus_strand_sorted.bed", "-r", "J:\\VishalData\\additional\\mouse_mm9.txt", "-o", "J:\\VishalData"
        };

        args = new String[]
        {
            "-c", "-r", "J:\\VishalData\\additional\\mouse_mm9.txt", "-1", "J:\\VishalData\\Ptbp2_Adult_testis_CLIP_mm9_plus_strand_sorted_BDB", "-2", "J:\\VishalData\\Ptbp2_E18_5_cortex_CLIP_mm9_plus_strand_sorted_BDB", "-w", "200", "-s", "50", "-o", "J:\\VishalData"
        };*/
        Options options = new Options();

        // add t option
        options.addOption("i", "index", false, "Indexing BED files.");
        options.addOption("b", "bed", true, "bed file to be indexed");
        options.addOption("o", "output", true, "Folder that the index/comparison file will be created.");
        options.addOption("r", "reference", true, "Reference annotation file to be used for indexing");
        options.addOption("c", "compare", false, "Finding differences between two conditions");
        options.addOption("1", "first", true, "First sample index location");
        options.addOption("2", "second", true, "Second sample index location");
        options.addOption("w", "window", true, "Length of window for identifying differences");
        options.addOption("s", "sliding", true, "Length of sliding");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        boolean indexing = false;
        boolean comparing = false;

        //Indexing!
        if (cmd.hasOption("i"))
        {
            //if(cmd.hasOption("1"))
            //System.err.println("sasan");

            //System.out.println("sasa");
            indexing = true;

        }
        else if (cmd.hasOption("c"))
        {
            //System.err.println("");
            comparing = true;

        }
        else
        {
            //System.err.println("Option is not deteced.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("diffhunter", options);
            return;
        }

        //Indexing is selected
        //
        if (indexing == true)
        {
            //Since indexing is true.
            //User have to provide file for indexing.
            if (!(cmd.hasOption("o") || cmd.hasOption("r") || cmd.hasOption("b")))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("diffhunter", options);
                return;
            }
            String bedfile_ = cmd.getOptionValue("b");
            String reference_file = cmd.getOptionValue("r");
            String folder_loc = cmd.getOptionValue("o");

            String sample_name = FilenameUtils.getBaseName(bedfile_);

            try (Database B2 = BerkeleyDB_Box.Get_BerkeleyDB(Paths.get(folder_loc, sample_name + "_BDB").toAbsolutePath().toString(), true, sample_name)) 
            {
                Indexer indexing_ = new Indexer(reference_file);
                indexing_.Make_Index(B2, bedfile_, Paths.get(folder_loc, sample_name + "_BDB").toAbsolutePath().toString());
                B2.close();

            }
        }
        else if (comparing == true)
        {
            if (!(cmd.hasOption("o") || cmd.hasOption("w") || cmd.hasOption("s") || cmd.hasOption("1") || cmd.hasOption("2")))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("diffhunter", options);
                return;
            }
            String folder_loc = cmd.getOptionValue("o");
            int window_ = Integer.parseInt(cmd.getOptionValue("w"));
            //int window_=600;

            int slide_ = Integer.parseInt(cmd.getOptionValue("s"));

            String first = cmd.getOptionValue("1").replace("_BDB", "");
            String second = cmd.getOptionValue("2").replace("_BDB", "");
            String reference_file = cmd.getOptionValue("r");
            //String folder_loc=cmd.getOptionValue("o");

            String sample_name_first = FilenameUtils.getBaseName(first);
            String sample_name_second = FilenameUtils.getBaseName(second);

            Database B1 = BerkeleyDB_Box.Get_BerkeleyDB(first + "_BDB", false, sample_name_first);
            Database B2 = BerkeleyDB_Box.Get_BerkeleyDB(second + "_BDB", false, sample_name_second);

            List<String> first_condition_genes = Files.lines(Paths.get(first + "_BDB", sample_name_first + ".txt").toAbsolutePath()).collect(Collectors.toList());
            List<String> second_condition_genes = Files.lines(Paths.get(second + "_BDB", sample_name_second + ".txt").toAbsolutePath()).collect(Collectors.toList());
            System.out.println("First and second condition are loaded!!! ");
            List<String> intersection_ = new ArrayList<>(first_condition_genes);
            intersection_.retainAll(second_condition_genes);

            BufferedWriter output = new BufferedWriter(new FileWriter(Paths.get(folder_loc, "differences_" + window_ + "_s" + slide_ + "_c" + ".txt").toAbsolutePath().toString(), true));
            List<Result_Window> final_results = Collections.synchronizedList(new ArrayList<>());
            Worker_New worker_class = new Worker_New();
            worker_class.Read_Reference(reference_file);

            while (!intersection_.isEmpty())
            {
                List<String> selected_genes = new ArrayList<>();
                //if (intersection_.size()<=10000){selected_genes.addAll(intersection_.subList(0, intersection_.size()));}
                //else selected_genes.addAll(intersection_.subList(0, 10000));
                if (intersection_.size() <= intersection_.size())
                {
                    selected_genes.addAll(intersection_.subList(0, intersection_.size()));
                }
                else
                {
                    selected_genes.addAll(intersection_.subList(0, intersection_.size()));
                }
                intersection_.removeAll(selected_genes);
                //System.out.println("Intersection count is:"+intersection_.size());
                //final List<Result_Window> resultssss_=new ArrayList<>();
                IntStream.range(0, selected_genes.size()).parallel().forEach(i ->
                {
                    System.out.println(selected_genes.get(i) + "\tprocessing......");
                    String gene_of_interest = selected_genes.get(i);//"ENSG00000142657|PGD";//intersection_.get(6);////"ENSG00000163395|IGFN1";//"ENSG00000270066|SCARNA2";
                    int start = worker_class.dic_genes.get(gene_of_interest).start_loc;
                    int end = worker_class.dic_genes.get(gene_of_interest).end_loc;

                    Map<Integer, Integer> first_ = Collections.EMPTY_MAP;
                    try
                    {
                        first_ = BerkeleyDB_Box.Get_Coord_Read(B1, gene_of_interest);
                    }
                    catch (IOException | ClassNotFoundException ex)
                    {
                        Logger.getLogger(DiffHunter.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Map<Integer, Integer> second_ = Collections.EMPTY_MAP;
                    try
                    {
                        second_ = BerkeleyDB_Box.Get_Coord_Read(B2, gene_of_interest);
                    }
                    catch (IOException | ClassNotFoundException ex)
                    {
                        Logger.getLogger(DiffHunter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    List<Window> top_windows_first = worker_class.Get_Top_Windows(window_, first_, slide_);
                    List<Window> top_windows_second = worker_class.Get_Top_Windows(window_, second_, slide_);
                    //System.out.println("passed for window peak call for gene \t"+selected_genes.get(i));
                    // System.out.println("top_window_first_Count\t"+top_windows_first.size());
                    // System.out.println("top_window_second_Count\t"+top_windows_second.size());
                    if (top_windows_first.isEmpty() && top_windows_second.isEmpty())
                    {
                        return;
                    }
                    List<Result_Window> res_temp = new Worker_New().Get_Significant_Windows(gene_of_interest, start, end, top_windows_first, top_windows_second, second_, first_, sample_name_first, sample_name_second, 0.01);
                    if (!res_temp.isEmpty())
                    {
                        final_results.addAll(res_temp);//final_results.addAll(worker_class.Get_Significant_Windows(gene_of_interest, start, end, top_windows_first, top_windows_second, second_, first_, first_condition, second_condition, 0.01));
                    }                //System.out.println(selected_genes.get(i)+"\tprocessed.");

                }
                );

                /*selected_genes.parallelStream().forEach(i ->
                 {
   

                 });*/
                System.out.println("Writing to file...");
                output.append("Gene_Symbol\tContributing_Sample\tStart\tEnd\tOddsRatio\tp_Value");
                output.newLine();
                for (Result_Window item : final_results)
                {
                    output.append(item.associated_gene_symbol + "\t" + item.contributing_windows + "\t" + item.start_loc + "\t" + item.end_loc + "\t" + item.oddsratio_ + "\t" + item.p_value); //+ "\t" + item.average_other_readcount_cotributing + "\t" + item.average_other_readcount_cotributing + "\t" + item.average_window_readcount_non + "\t" + item.average_other_readcount_non);
                    output.newLine();
                }
                final_results.clear();

            }

        }
        System.out.println("Done.");

    }

}
