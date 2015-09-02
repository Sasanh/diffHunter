/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffhunter;

import org.apache.commons.io.FilenameUtils;
import com.sleepycat.je.Database;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import intervalTree.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author hashemis
 */
public class Indexer
{

    Map<String, AbstractMap.SimpleEntry<Integer, Integer>> dic_transcripts_locs = new HashMap<>();
    Map<String, Object> lock_dic_genes = new HashMap<>();
    Map<String, List<String>> dic_gene_transcripts = new HashMap<>();
    public HashMap<String, HashMap<String, IntervalTree<String>>> dic_Loc_gene;
    public HashMap<String, Gene> dic_genes = new HashMap<>();
    private HashMap<String, Object> dic_synchrinzer_genes = new HashMap<>();
//   indexing_.dic_Loc_gene = Worker_New.get_gene_coords_updated(indexing_.dic_genes, "J:\\VishalData\\additional\\HG19_Gene_Transcripts.txt", dic_gene_transcripts, dic_transcripts_locs, lock_dic_genes);
    public Indexer(String location_) throws IOException
    {
        this.dic_Loc_gene = new HashMap<>();
        dic_Loc_gene=get_gene_coords_updated(dic_genes, location_, dic_gene_transcripts, dic_transcripts_locs, lock_dic_genes);
        
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
    private void Set_Parameters()
    {

        dic_synchrinzer_genes.clear();
        Set<String> keys_ = dic_genes.keySet();
        keys_.stream().forEach((inner_keyKeys_) ->
        {
            dic_synchrinzer_genes.put(inner_keyKeys_, Boolean.TRUE);
        });
    }

    public void Make_Index(Database hashdb, String file_name,String read_gene_location) throws FileNotFoundException, IOException
    {
        Set_Parameters();
        //System.out.print("Sasa");
        ConcurrentHashMap<String, Map<Integer, Integer>> dic_gene_loc_count = new ConcurrentHashMap<>();
        ArrayList<String> lines_from_bed_file = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file_name));

        String line = br.readLine();
        List<String> toks = Arrays.asList(line.split("\t"));
        lines_from_bed_file.add(line);
        String last_Seen_chromosome = toks.get(0).replace("chr", "");
        line = br.readLine();
        lines_from_bed_file.add(line);
        toks = Arrays.asList(line.split("\t"));
        String new_chromosome = toks.get(0).replace("chr", "");

        while (((line = br.readLine()) != null) || lines_from_bed_file.size() > 0)
        {
            if (line != null)
            {
                toks = Arrays.asList(line.split("\t"));
                new_chromosome = toks.get(0).replace("chr", "");
            }
            // process the line.
            if (line == null || !new_chromosome.equals(last_Seen_chromosome))
            {
                System.out.println("Processing chromosome"+"\t"+last_Seen_chromosome);
                last_Seen_chromosome = new_chromosome;
                lines_from_bed_file.parallelStream().forEach(content ->
                {

                    List<String> inner_toks = Arrays.asList(content.split("\t"));
                        //WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING WARNINNG WARNING WARNING 
                    //STRAND column count should be changed. 
                    String strand = inner_toks.get(5);
                    String chromosome_ = inner_toks.get(0).replace("chr", "");
                    if (!dic_Loc_gene.get(strand).containsKey(chromosome_))
                    {
                        return;
                    }
                    Integer start_loc = Integer.parseInt(inner_toks.get(1));
                    Integer end_loc = Integer.parseInt(inner_toks.get(2));
                    List<Interval<String>> res__ = dic_Loc_gene.get(strand).get(chromosome_).getIntervals(start_loc, end_loc);
                         //IntervalTree<String> pot_gene_name=new IntervalTree<>(res__);
                    //                        for (int z = 0; z < pot_gene_name.Intervals.Count; z++)
                    //{
                    for (int z = 0; z < res__.size(); z++)
                    {

                        dic_gene_loc_count.putIfAbsent(res__.get(z).getData(), new HashMap<>());
                        String gene_symbol = res__.get(z).getData();
                        Integer temp_gene_start_loc = dic_genes.get(gene_symbol).start_loc;
                        Integer temp_gene_end_loc = dic_genes.get(gene_symbol).end_loc;
                        if (start_loc < temp_gene_start_loc)
                        {
                            start_loc = temp_gene_start_loc;
                        }
                        if (end_loc > temp_gene_end_loc)
                        {
                            end_loc = temp_gene_end_loc;
                        }
                        synchronized (dic_synchrinzer_genes.get(gene_symbol))
                        {
                            for (int k = start_loc; k <= end_loc; k++)
                            {
                                Integer value_inside = 0;
                                value_inside = dic_gene_loc_count.get(gene_symbol).get(k);
                                dic_gene_loc_count.get(gene_symbol).put(k, value_inside == null ? 1 : (value_inside + 1));
                            }
                        }
                    }
                });
                /*                    List<string> keys_ = dic_gene_loc_count.Keys.ToList();
                 List<string> alt_keys = new List<string>();// dic_gene_loc_count.Keys.ToList();
                 for (int i = 0; i < keys_.Count; i++)
                 {
                 Dictionary<int, int> dicccc_ = new Dictionary<int, int>();
                 dic_gene_loc_count[keys_[i]] = new Dictionary<int, int>(dic_gene_loc_count[keys_[i]].Where(x => x.Value >= 2).ToDictionary(x => x.Key, x => x.Value));
                 if (dic_gene_loc_count[keys_[i]].Count == 0)
                 {

                 dic_gene_loc_count.TryRemove(keys_[i], out dicccc_);
                 continue;
                 }
                 hashdb.Put(Get_BDB(keys_[i]), Get_BDB_Dictionary(dic_gene_loc_count[keys_[i]]));
                 alt_keys.Add(keys_[i]);
                 dic_gene_loc_count.TryRemove(keys_[i], out dicccc_);
                 }*/
                ArrayList<String> keys_ = new ArrayList<>(dic_gene_loc_count.keySet());
                ArrayList<String> alt_keys = new ArrayList<>();
                for (int i = 0; i < keys_.size(); i++)
                {

                    //LinkedHashMap<Integer, Integer> tmep_map = new LinkedHashMap<>(dic_gene_loc_count.get(keys_.get(i)));
                     LinkedHashMap<Integer, Integer> tmep_map=new LinkedHashMap<>();
                    /*tmep_map = */
                    dic_gene_loc_count.get(keys_.get(i)).entrySet().stream().filter(p -> p.getValue() >= 2).sorted(Comparator.comparing(E -> E.getKey()))
                                                                   .forEach((entry) -> tmep_map.put(entry.getKey(), entry.getValue()));//.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
                    if (tmep_map.isEmpty())
                    {
                        dic_gene_loc_count.remove(keys_.get(i));
                        continue;
                    }

                    //Map<Integer, Integer> tmep_map1 = new LinkedHashMap<>();
                    //tmep_map1=sortByKey(tmep_map);
                    //tmep_map.entrySet().stream().sorted(Comparator.comparing(E -> E.getKey())).forEach((entry) -> tmep_map1.put(entry.getKey(), entry.getValue()));
                    //BerkeleyDB_Box box=new BerkeleyDB_Box();
                    hashdb.put(null, BerkeleyDB_Box.Get_BDB(keys_.get(i)), BerkeleyDB_Box.Get_BDB_Dictionary(tmep_map));
                    alt_keys.add(keys_.get(i));
                    dic_gene_loc_count.remove(keys_.get(i));
                    //dic_gene_loc_count.put(keys_.get(i),tmep_map);
                }

                hashdb.sync();
                int a = 1111;
                /*                    hashdb.Sync();
                 File.AppendAllLines("InputDB\\" + Path.GetFileNameWithoutExtension(file_name) + "_genes.txt", alt_keys);
                 //total_lines_processed_till_now += lines_from_bed_file.Count;
                 //worker.ReportProgress(total_lines_processed_till_now / count_);
                 lines_from_bed_file.Clear();
                 if (!reader.EndOfStream)
                 {
                 lines_from_bed_file.Add(_line_);
                 }
                 last_Seen_chromosome = new_choromosome;*/
                lines_from_bed_file.clear();
                if (line != null)
                {
                    lines_from_bed_file.add(line);
                }
                Path p=Paths.get(file_name);
                file_name=p.getFileName().toString();
                
                BufferedWriter output = new BufferedWriter(new FileWriter((Paths.get(read_gene_location,FilenameUtils.removeExtension(file_name)+".txt").toString()), true));
                for (String alt_key : alt_keys)
                {
                    output.append(alt_key);
                    output.newLine();
                }
                output.close();
                /*if (((line = br.readLine()) != null))
                {
                lines_from_bed_file.add(line);
                toks=Arrays.asList(line.split("\t"));
                new_chromosome=toks.get(0).replace("chr", "");
                }*/
                //last_Seen_chromosome=new_chromosome;
            }
            else if (new_chromosome.equals(last_Seen_chromosome))
            {
                lines_from_bed_file.add(line);
            }

        }
        br.close();
        hashdb.sync();
        hashdb.close();

    }

    public static <K extends Comparable<? super K>, V> Map<K, V>
            sortByKey(Map<K, V> map)
    {
        Map<K, V> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Comparator.comparing(E -> E.getKey())).forEach((entry) -> result.put(entry.getKey(), entry.getValue()));

        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<K, V> o1, Map.Entry<K, V> o2) -> (o1.getKey()).compareTo(o2.getKey()));

        list.stream().forEach((entry) ->
        {
            result.put(entry.getKey(), entry.getValue());
        });
        return result;
    }

}
