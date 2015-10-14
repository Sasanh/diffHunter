/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diffhunter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The code is inspired from Pavlidis Lab, BaseCode Project. 
 * @author hashemis
 */
public class MultipleTestCorrection
{
        public static ArrayList<Double> benjaminiHochberg(List<Double> pvalues)
    {
        int nump = pvalues.size();
        int n = nump;
        
        
        ArrayList<AbstractMap.SimpleEntry<Double, Integer>> dic_array_index = new ArrayList<>();
        for (int i = 0; i < pvalues.size(); i++)
        {
            dic_array_index.add(new AbstractMap.SimpleEntry<>(pvalues.get(i), i));
        }


        
        Comparator<AbstractMap.SimpleEntry<Double, Integer>> byKey_ = ((e1, e2) -> e1.getKey().compareTo(e2.getKey()));
        List<AbstractMap.SimpleEntry<Double, Integer>> tt = dic_array_index.stream().sorted(byKey_).collect(Collectors.toList());
        List<Double> sorted = tt.stream().map(x -> x.getKey()).collect(Collectors.toList());
        List<Integer> order = tt.stream().map(x -> x.getValue()).collect(Collectors.toList());

        ArrayList<Double> tmp = new ArrayList<>(nump);
        for (int i = 0; i < nump; i++)
        {
           tmp.add(0.0);
        }

       // ArrayList<Double> sorted = new ArrayList<>(values);


        double previous = 1.0;
        for (int i = sorted.size() - 1; i >= 0; i--)
        {
            double pval = sorted.get(i);
         
            double qval = Math.min(pval * nump / n, previous);
         
            tmp.set(i, qval);
            previous = qval;
            n--;
        }
        ArrayList<Double> results = new ArrayList<>(nump);
        for (int i = 0; i < nump; i++)
        {
            results.add(0.0);
        }
        for (int i = 0; i < nump; i++)
        {
            results.set(order.get(i), tmp.get(i));
        }

        return results;

        //return new ArrayList<>();
    }
  
}
