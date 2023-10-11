import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Timer {

    private final Processor processor;

    private JPanel RootPanel;
    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JLabel timeField;

    public Timer() {
        timeField.setText("0 : 0");
        processor = new Processor(timeField);
        processor.setDaemon(true);
        processor.start();

        startButton.addActionListener(e -> {
            processor.startOrResume();
            startButton.setVisible(false);
            pauseButton.setVisible(true);
        });
        pauseButton.addActionListener(e -> {
            pauseButton.setVisible(false);
            startButton.setVisible(true);
            processor.timePause();
        });
        stopButton.addActionListener(e -> {
            pauseButton.setVisible(false);
            startButton.setVisible(true);
            processor.timeStop();
        });
    }

    public JPanel getRootPanel() {
        return RootPanel;
    }
}
