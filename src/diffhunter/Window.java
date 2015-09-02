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
    public class Window
    {
        public Integer start_wnd;
        public Integer end_wnd;
        public Integer score_ = 0;
        public Integer Length()
        { return end_wnd - start_wnd + 1; }
    }