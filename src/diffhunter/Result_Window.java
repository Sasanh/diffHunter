/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffhunter;

/**
 *
 * @author hashemis
 */
   public class Result_Window
    {
        public String associated_gene_symbol = "";
        public Double p_value;
        public Double oddsratio_;
        public Integer start_loc;
        public Integer end_loc;
        public Double average_window_readcount_cotributing = 0.0;
        public Double average_other_readcount_cotributing = 0.0;
        public Double average_window_readcount_non = 0.0;
        public Double average_other_readcount_non = 0.0;


        public String contributing_windows = "";
    }
