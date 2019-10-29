package com.ToppInd;

import javax.swing.*;
import java.awt.*;

final class Prefab {
    static class Button {
        static JButton getButton() {
            var button = new JButton("Configure Prefab");
            button.addActionListener(e -> Window.display());
            return button;
        }
    }

    static class Window {
        static void display() {
            var window = new JFrame("Prefab Configurer");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            window.setVisible(true);
        }
    }
}
