package com.klemstinegroup;

import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
            	System.out.println(e.getKeyCode());
            	if (e.getKeyCode()==KeyEvent.VK_M)Main.mustacheOn=!Main.mustacheOn;
        		if (e.getKeyCode()==KeyEvent.VK_F11){
        			Toolkit.getDefaultToolkit().beep();
        		}
            }
            return false;
        }
    }