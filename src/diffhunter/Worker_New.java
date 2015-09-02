    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffhunter;

import intervalTree.IntervalTree;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jsc.contingencytables.ContingencyTable2x2;
import jsc.contingencytables.FishersExactTest;

/**
 *
 * @author hashemis
 */
public class Worker_New
{

    public Map<String, AbstractMap.SimpleEntry<Integer, Integer>> dic_transcripts_locs = new HashMap<>();
    public Map<String, Object> lock_dic_genes = new HashMap<>();
    public Map<String, List<String>> dic_gene_transcripts = new HashMap<>();
    public HashMap<String, HashMap<String, IntervalTree<String>>> dic_Loc_gene;
    public HashMap<String, Gene> dic_genes = new HashMap<>();

    public List<Result_Window> Process_Approach(Map<Integer, Integer> dic_first, Map<Integer, Integer> dic_second, String gene_of_interest, double Window_Size, String first_window_name, String second_window_name, int begin, int end, double p_value_threshold, int slide_)
    {
        Map<Integer, Integer> first_ = dic_first;
        Map<Integer, Integer> second_ = dic_first;

        List<Window> top_window_first = Get_Top_Windows((int) Window_Size, first_, slide_);
        List<Window> top_window_second = Get_Top_Windows((int) Window_Size, second_, slide_);

        return Get_Significant_Windows(gene_of_interest, begin, end, top_window_first, top_window_second, second_, first_, first_window_name, second_window_name, p_value_threshold);

    }

    public List<Window> Get_Top_Windows(Integer window_size, Map<Integer, Integer> Gene_read_counts, int slide_)
    {
        if (Gene_read_counts.isEmpty())
        {
            return new ArrayList<>();
        }
        //System.out.println("Passed Gene read count");
        int max_ = Gene_read_counts.keySet().stream().mapToInt(i -> i).max().getAsInt();
        int min_ = Gene_read_counts.keySet().stream().mapToInt(i -> i).min().getAsInt();

        List<Window> sorted_windows_list = new ArrayList<>();
        /*if (max_ - min_ > 12000*slide_)
        {
            // System.out.println("Entered >12000");
            sorted_windows_list = Parallel_Top_Windows_Helper(min_, max_, window_size, Gene_read_counts, slide_);
        }
        else if (max_ - min_ > 4000*slide_ && window_size > 700)
        {
            //System.out.println("Entered >4000");
            sorted_windows_list = Parallel_Top_Windows_Helper(min_, max_, window_size, Gene_read_counts, slide_);
        }
        else
        {
            //System.out.println("Enteretd else ");
            sorted_windows_list = Serial_Top_Windows_Helper(min_, max_, window_size, Gene_read_counts, slide_);
        }*/
//ADDED LATER ON FEB 10th 2015 should be removed later IF didnt satisfy the purposes.
        sorted_windows_list = Serial_Top_Windows_Helper(min_, max_, window_size, Gene_read_counts, slide_);
        return sorted_windows_list;
    }

    //public List<Window> Serial_Top_Windows_Helper(int min_, int max_, int window_size, Dictionary<int, int> Gene_read_counts)
    public List<Window> Serial_Top_Windows_Helper(Integer min_, Integer max_, Integer window_size, Map<Integer, Integer> Gene_read_counts, int slide_)
    {
        ArrayList<Window> final_window_list = new ArrayList<>();
        if (max_ - min_ < window_size)
        {
            max_ = min_ + window_size;
        }

        for (int i = min_; i < max_ + 1; i = i + slide_)
        {
            if (i + window_size > max_)
            {
                continue;
            }
            Integer total_sum_per_window = 0;
            for (int j = i; j < i + window_size; j++)
            {
                Integer basepoint = 0;
                basepoint = Gene_read_counts.get(j);
                if (basepoint != null)
                {
                    total_sum_per_window += basepoint + 1;
                }
                else
                {
                    total_sum_per_window += 1;
                }

            }
            Window temp_wnd = new Window();
            temp_wnd.score_ = total_sum_per_window;
            temp_wnd.start_wnd = i;
            temp_wnd.end_wnd = i + window_size - 1;
            if (total_sum_per_window >= window_size * 2)
            {
                //Integer found_index=final_window_list.stream().
                int[] toArray = IntStream.range(0, final_window_list.size())
                        .filter(x
                                -> (final_window_list.get(x).start_wnd <= temp_wnd.start_wnd && final_window_list.get(x).end_wnd >= temp_wnd.start_wnd) || (final_window_list.get(x).start_wnd <= temp_wnd.end_wnd && final_window_list.get(x).end_wnd >= temp_wnd.end_wnd))
                        .toArray();
                if (toArray.length > 0)
                {
                    if (final_window_list.get(toArray[0]).score_ < temp_wnd.score_)
                    {
                        final_window_list.remove(toArray[0]);
                        final_window_list.add(temp_wnd);
                    }
                }
                else
                {
                    final_window_list.add(temp_wnd);
                }
            }
        }
        //   return final_window_list.OrderByDescending(x => x.score_).ThenBy(x => x.start_wnd).ToList();

        Comparator<Window> byScore_ = ((e1, e2) -> e1.score_.compareTo(e2.score_));
        Comparator<Window> byStart_wnd = (e1, e2) -> e1.start_wnd.compareTo(e2.start_wnd);
        return final_window_list.stream().sorted(byScore_.reversed().thenComparing(byStart_wnd)).collect(Collectors.toList());
//return  final_window_list.stream().sorted();
    }

    public List<Window> Parallel_Top_Windows_Helper(Integer min_1, Integer max_1, Integer window_size_1, Map<Integer, Integer> Gene_read_counts, int slide_)
    {
        //ArrayList<Window> final_window_list = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Window>());
        //List<Result_Window> final_results = Collections.synchronizedList(new ArrayList<>());
        List<Window> final_window_list = Collections.synchronizedList(new ArrayList<>());

        //int maximum_val
        if (max_1 - min_1 < window_size_1)
        {
            max_1 = min_1 + window_size_1;
        }
        int max_ = max_1;
        int min_ = min_1;
        int window_size = window_size_1;

        Object object = new Object();
        IntStream.iterate(min_, x -> x + slide_).parallel().limit((long) Math.abs((double) max_ % (double) slide_ == 0 ? max_ / slide_ : ((double) (max_) / (double) slide_) + 1)).forEach(i ->
        {
            if (i + window_size > max_)
            {
                return;
            }
            Integer total_sum_per_window = 0;
            for (int j = i; j < i + window_size; j++)
            {
                Integer basepoint = 0;
                basepoint = Gene_read_counts.get(j);
                if (basepoint != null)
                {
                    total_sum_per_window += basepoint + 1;
                }
                else
                {
                    total_sum_per_window += 1;
                }

            }
            Window temp_wnd = new Window();
            temp_wnd.score_ = total_sum_per_window;
            temp_wnd.start_wnd = i;
            temp_wnd.end_wnd = i + window_size - 1;
           
            if (total_sum_per_window >= window_size * 2)
            {
                synchronized (object)
                {
                    final_window_list.add(temp_wnd);
                }
            }
        });

        if (final_window_list.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        Comparator<Window> byScore_ = ((e1, e2) -> e1.score_.compareTo(e2.score_));
        Comparator<Window> byStart_wnd = (e1, e2) -> e1.start_wnd.compareTo(e2.start_wnd);
        //Parallel or not Parallel?!!!???!!!
        //long startTime = System.nanoTime();    
        // ... the code being measured ...    
        //System.out.println("Sorting started for top windows");
        List<Window> final_window_list1 = final_window_list.stream().sorted(byScore_.reversed().thenComparing(byStart_wnd)).collect(Collectors.toList());
        //System.out.println("Sorting done for top windows");
        //long estimatedTime = System.nanoTime() - startTime;
        /*startTime = System.nanoTime(); 
         List<Window> final_window_list2=final_window_list.parallelStream().sorted(byScore_.reversed().thenComparing(byStart_wnd)).collect(Collectors.toList());
         long estimatedTime1 = System.nanoTime() - startTime;*/
        //ArrayList<Integer> sasasasasa=new ArrayList<>();

        ArrayList<Window> trimmed_results = new ArrayList<>();
        //ArrayList<Window> test_final_ = new ArrayList<>();
        trimmed_results.add(final_window_list1.get(0));
        for (int i = 1; i < final_window_list1.size(); i++)
        {
            Window temp_temp = final_window_list1.get(i);
            //(x.start_wnd <= final_window_list1.get(i).start_wnd && x.end_wnd >= final_window_list1.get(i).start_wnd)|| (x.start_wnd <= final_window_list1.get(i).end_wnd && x.end_wnd >= final_window_list1.get(i).end_wnd)
            int count__ = trimmed_results.stream().filter(x -> (x.start_wnd <= temp_temp.start_wnd && x.end_wnd >= temp_temp.start_wnd) || (x.start_wnd <= temp_temp.end_wnd && x.end_wnd >= temp_temp.end_wnd)).collect(Collectors.toList()).size();
            //int count__ = test_final_.stream().filter(x -> (x.start_wnd < final_window_list.get(i).start_wnd && x.end_wnd > final_window_list.get(i).start_wnd) || (x.start_wnd <= final_window_list.get(i).end_wnd && x.end_wnd > final_window_list.get(i).end_wnd)).collect(Collectors.toList()).size();
            if (count__ == 0)
            {
                trimmed_results.add(final_window_list1.get(i));
            }
            //if ((final_window_list[i].start_wnd < test_final_[test_final_.Count - 1].end_wnd && final_window_list[i].start_wnd > test_final_[test_final_.Count - 1].start_wnd) || (final_window_list[i].end_wnd < test_final_[test_final_.Count - 1].end_wnd && final_window_list[i].end_wnd > test_final_[test_final_.Count - 1].start_wnd)) continue;
            //else test_final_.Add(final_window_list[i]);
        }

        // System.out.println("trimmed done!!! windos are ready!");

        /*while(!final_window_list1.isEmpty())
         {
         Window test_=final_window_list1.get(0);
         trimmed_results.add(final_window_list1.get(0));
         final_window_list1.remove(0);

            
            
         final_window_list1.removeAll(final_window_list1.stream().filter(x -> (x.start_wnd <= test_.start_wnd && x.end_wnd >= test_.start_wnd) || (x.start_wnd <= test_.end_wnd && x.end_wnd >= test_.end_wnd)).collect(Collectors.toList()));
            

         }
         */
        return trimmed_results;//final_window_list.stream().sorted(byScore_.reversed().thenComparing(byStart_wnd)).collect(Collectors.toList());

    }

    /* public void Significant_Windows_Helper(string gene_of_interest,int Gene_Start_loc, int Gene_End_loc, Window Highest_, Dictionary<int, int> Highest_One_Gene_ReadCount, Dictionary<int, int> Other_One_Gene_ReadCount, ref List<Window> Highest_One_Windows, ref List<Window> Other_One_Windows, ref List<Result_Window> dic_final_window_results, string window_name, double p_val_threshold)
     {
     */
    /*  public List<Result_Window> Get_Significant_Windows(string gene_of_interest,int start_loc, int end_loc, ref List<Window> dic_gene_TopWindows_first, ref List<Window> dic_gene_TopWindows_second, Dictionary<int, int> second_loc_readcount, Dictionary<int, int> first_loc_readcount, string first_window_name, string second_window_name, double p_val_threshold)
     */
    public List<Result_Window> Get_Significant_Windows(String gene_of_interest, int start_loc, int end_loc, List<Window> dic_gene_TopWindows_first, List<Window> dic_gene_TopWindows_second, Map<Integer, Integer> second_loc_readcount, Map<Integer, Integer> first_loc_readcount, String first_window_name, String second_window_name, double p_val_threshold)
    {
    
 
        List<Result_Window> dic_final_window_results = new ArrayList<>();
        while (!dic_gene_TopWindows_first.isEmpty() || !dic_gene_TopWindows_second.isEmpty())
        {
            if (dic_gene_TopWindows_first.isEmpty())
            {
                Significant_windows_Helper(gene_of_interest, start_loc, end_loc, dic_gene_TopWindows_second.get(0), second_loc_readcount, first_loc_readcount, dic_gene_TopWindows_second, dic_gene_TopWindows_first, dic_final_window_results, second_window_name, p_val_threshold);
            }
            else if (dic_gene_TopWindows_second.isEmpty())
            {
                Significant_windows_Helper(gene_of_interest, start_loc, end_loc, dic_gene_TopWindows_first.get(0), first_loc_readcount, second_loc_readcount, dic_gene_TopWindows_first, dic_gene_TopWindows_second, dic_final_window_results, first_window_name, p_val_threshold);
            }
            else if (dic_gene_TopWindows_first.get(0).score_ >= dic_gene_TopWindows_second.get(0).score_)
            {
                Significant_windows_Helper(gene_of_interest, start_loc, end_loc, dic_gene_TopWindows_first.get(0), first_loc_readcount, second_loc_readcount, dic_gene_TopWindows_first, dic_gene_TopWindows_second, dic_final_window_results, first_window_name, p_val_threshold);
            }
            else
            {
                Significant_windows_Helper(gene_of_interest, start_loc, end_loc, dic_gene_TopWindows_second.get(0), second_loc_readcount, first_loc_readcount, dic_gene_TopWindows_second, dic_gene_TopWindows_first, dic_final_window_results, second_window_name, p_val_threshold);
            }
        }
        return dic_final_window_results;
    }

    public void Significant_windows_Helper(String gene_of_interest, int Gene_Start_loc, int Gene_End_loc, Window Highest_, Map<Integer, Integer> Highest_One_Gene_ReadCount, Map<Integer, Integer> Other_One_Gene_ReadCount, List<Window> Highest_One_Windows, List<Window> Other_One_Windows, List<Result_Window> dic_final_window_results, String window_name, double p_val_threshold)
    {

        int region_ = Gene_End_loc - Gene_Start_loc;
        if (region_ < Highest_.Length())
        {
            region_ = Highest_.Length();
        }
        int score_window_this = Highest_.score_;
        int score_total_this = (region_ + 1) - Highest_.Length() + (Highest_One_Gene_ReadCount.values().stream().mapToInt(i -> i).sum() - score_window_this + Highest_.Length());
        int score_window_other = 0;
        int score_total_other = 0;
        for (int i = Highest_.start_wnd; i <= Highest_.end_wnd; i++)
        {
            Integer temp_loc_val = 0;
            temp_loc_val = Other_One_Gene_ReadCount.get(i);
            if (temp_loc_val == null)
            {
                score_window_other += 1;
            }
            else
            {
                score_window_other += temp_loc_val + 1;
            }
        }

        score_total_other = (region_ + 1) - Highest_.Length() + (Other_One_Gene_ReadCount.values().stream().mapToInt(Integer::intValue).sum() - score_window_other + Highest_.Length());
        //System.out.println("Entering Fisher");
        //FisherExact fisher_test = new FisherExact(score_total_this + score_total_other + score_window_other + score_window_this + 10);
        //System.out.println("Out from fisher");
        double oddsratio = OddsRatio(score_window_this, score_total_this, score_window_other, score_total_other);
        //double p_val = fisher_test.getTwoTailedP(score_window_this, score_total_this, score_window_other, score_total_other);
        ContingencyTable2x2 table1 = new ContingencyTable2x2(score_window_this, score_total_this, score_window_other, score_total_other);
        FishersExactTest testttt = new FishersExactTest(table1);
        double p_val = testttt.getApproxSP();//.getSP();

        Result_Window window_ = new Result_Window();
        window_.associated_gene_symbol = "";
        window_.p_value = p_val;
        if (window_.p_value > p_val_threshold)
        {
            Highest_One_Windows.remove(0);
            return;
        }

        window_.associated_gene_symbol = gene_of_interest;
        window_.oddsratio_ = oddsratio;
        window_.end_loc = Highest_.end_wnd;
        window_.start_loc = Highest_.start_wnd;
        window_.contributing_windows = window_name;
        window_.average_window_readcount_cotributing = (double) Highest_.score_ / (double) Highest_.Length();
        window_.average_other_readcount_cotributing = (double) score_total_this / (double) (region_ + 1 - Highest_.Length());
        window_.average_window_readcount_non = (double) score_window_other / (double) Highest_.Length();
        window_.average_other_readcount_non = (double) score_total_other / (double) (region_ + 1 - Highest_.Length());
        dic_final_window_results.add(window_);

        //NOW WE HAVE TO WRITE THE INDEX finding. I already found the solution. it is in the main function of this program. 
        int index__ = Other_One_Windows.indexOf(Other_One_Windows.stream().filter(t -> t.start_wnd <= window_.start_loc && t.end_wnd >= window_.start_loc).reduce((previous, current) -> current).orElse(new Window()));
        while (index__ != -1)
        {
            Other_One_Windows.remove(index__);
            index__ = Other_One_Windows.indexOf(Other_One_Windows.stream().filter(t -> t.start_wnd <= window_.start_loc && t.end_wnd >= window_.start_loc).reduce((previous, current) -> current).orElse(new Window()));
        }
        Highest_One_Windows.remove(0);
    }
    /*      public void Read_Reference(string ref_file_name)
     {
     dic_Loc_gene = get_gene_coords_updated(ref dic_genes, ref_file_name,ref dic_gene_transcripts,ref dic_transcript_locs,ref lock_dic_genes);
     }*/

    public void Read_Reference(String ref_file_name) throws IOException
    {
        dic_Loc_gene = get_gene_coords_updated(dic_genes, ref_file_name, dic_gene_transcripts, dic_transcripts_locs, lock_dic_genes);
    }

    double OddsRatio(int a, int b, int c, int d)
    {
        // double r = (1.0 * this[0, 0] / this[0, 1]) / (1.0 * this[1, 0] / this[1, 1]);
        return (1.0 * (double) a / (double) b) / ((double) c / (double) d);
    }

    private
            HashMap<String, HashMap<String, IntervalTree<String>>> get_gene_coords_updated(Map<String, Gene> dic_genes, String file_name,
                    Map<String, List<String>> dic_gene_transcripts,
                    Map<String, AbstractMap.SimpleEntry<Integer, Integer>> dic_transcripts_locs,
                    Map<String, Object> lock_dic_genes) throws FileNotFoundException, IOException
    {
        /*            Dictionary<string, Dictionary<string, IntervalTree<string, int>>> dic_Loc_gene = new Dictionary<string, Dictionary<string, IntervalTree<string, int>>>();
         dic_Loc_gene["-"] = new Dictionary<string, IntervalTree<string, int>>();
         dic_Loc_gene["+"] = new Dictionary<string, IntervalTree<string, int>>();*/
        HashMap<String, HashMap<String, IntervalTree<String>>> dic_Loc_gene = new HashMap<>();
        dic_Loc_gene.put("+", new HashMap<>());
        dic_Loc_gene.put("-", new HashMap<>());
        /*            StreamReader reader = new StreamReader(file_name);
         reader.ReadLine();
         HashSet<string> processed_genes = new HashSet<string>();*/
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        String line = br.readLine();
        HashSet<String> processed_Genes = new HashSet<>();

        while (((line = br.readLine()) != null))
        {
            List<String> toks = Arrays.asList(line.split("\t"));
            /*
             Gene temp = new Gene();
             //temp.chromosome = toks[1];//.Replace("_random", "");
             temp.chromosome = toks[8];
             //temp.symbol = toks[0];
             //TEMP CHANGE WARNIING WARNING WARNING WARNING WARNING WARNIING WARNING WARNING WARNING WARNING WARNIING WARNING WARNING WARNING WARNING WARNIING WARNING WARNING WARNING WARNING WARNIING WARNING WARNING WARNING WARNING 
             //temp.symbol = toks[1] + "|" + toks[0];
             temp.symbol = toks[0] + "|" + toks[1];
             */
            Gene temp = new Gene();
            temp.chromosome = toks.get(8).replace("chr", "");
            temp.symbol = toks.get(0) + "|" + toks.get(1);
            /*                string gene_name = temp.symbol;
             string trasncript = toks[2];
             if (!dic_gene_transcripts.ContainsKey(gene_name)) dic_gene_transcripts.Add(gene_name, new List<string>());
             if (!dic_transcript_locs.ContainsKey(trasncript)) dic_transcript_locs.Add(trasncript, new KeyValuePair<int, int>(int.Parse(toks[5]), int.Parse(toks[6])));
             dic_gene_transcripts[gene_name].Add(trasncript);*/
            String gene_name = temp.symbol;
            String trasncript = toks.get(2);
            if (!dic_gene_transcripts.containsKey(gene_name))
            {
                dic_gene_transcripts.put(gene_name, new ArrayList<>());
            }
            if (!dic_transcripts_locs.containsKey(trasncript))
            {
                dic_transcripts_locs.put(trasncript, new AbstractMap.SimpleEntry<>(Integer.parseInt(toks.get(5)), Integer.parseInt(toks.get(6))));
            }
            dic_gene_transcripts.get(gene_name).add(trasncript);
            /*          if (!processed_genes.Contains(temp.symbol)) processed_genes.Add(temp.symbol);
             else continue;
             temp.strand = toks[7] == "1" ? "+" : "-";
             //The additions and deductions are based on to map the reads in the start and end of genes. 
             temp.start_loc = int.Parse(toks[3]);// -200;
             temp.end_loc = int.Parse(toks[4]);// +200;*/

            if (!processed_Genes.contains(temp.symbol))
            {
                processed_Genes.add(temp.symbol);
            }
            else
            {
                continue;
            }
            temp.strand = "1".equals(toks.get(7)) ? "+" : "-";
            temp.start_loc = Integer.parseInt(toks.get(3));
            temp.end_loc = Integer.parseInt(toks.get(4));

            /*                if (!dic_Loc_gene[temp.strand].ContainsKey(temp.chromosome)) dic_Loc_gene[temp.strand].Add(temp.chromosome, new IntervalTree<string, int>());
             //for start
             dic_Loc_gene[temp.strand][temp.chromosome].AddInterval(temp.start_loc, temp.end_loc, temp.symbol);
             //for end
             //dic_Loc_gene[temp.strand][temp.chromosome][temp.start_loc][temp.end_loc] = temp.symbol;
             //temp.start_loc = temp.start_loc + 200;
             //temp.end_loc = temp.end_loc - 200;
             dic_genes[temp.symbol] = temp;
             lock_dic_genes[temp.symbol] = true;*/
            if (!dic_Loc_gene.get(temp.strand).containsKey(temp.chromosome))
            {
                dic_Loc_gene.get(temp.strand).put(temp.chromosome, new IntervalTree<>());
            }
            dic_Loc_gene.get(temp.strand).get(temp.chromosome).addInterval(temp.start_loc, temp.end_loc, temp.symbol);
            dic_genes.put(temp.symbol, temp);
            lock_dic_genes.put(temp.symbol, true);

        }

        return dic_Loc_gene;

    }
}
