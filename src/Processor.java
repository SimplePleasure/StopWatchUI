import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class Processor extends Thread {

    private static final int UPDATE_FREQUENCY_MS = 100;
    private static final String FORMAT_TIME = "%d : %03d";
    private final JLabel timeField;

    private volatile LocalDateTime timerStarted;
    private volatile boolean stopWatchStarted;

    private Duration period;

    Processor(JLabel timeField) {
        this.timeField = timeField;
        this.stopWatchStarted = false;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                if (!stopWatchStarted) {
                    wait();
                }
                updateTime();
                Thread.sleep(UPDATE_FREQUENCY_MS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    void startOrResume() {
        if (timerStarted == null) {
            stopWatchStarted = true;
            timerStarted = LocalDateTime.now();
        } else {
            stopWatchStarted = true;
            timerStarted = LocalDateTime.now().minusNanos(period.toNanos());
        }
    }

    void timePause() {
        stopWatchStarted = false;
    }

    void timeStop() {
        stopWatchStarted = false;
        timerStarted = null;
        SwingUtilities.invokeLater(() -> timeField.setText("0 : 0"));
    }

    private void updateTime() {
        period = Duration.between(timerStarted, LocalDateTime.now());
        String result = String.format(FORMAT_TIME, period.toSeconds(), period.toMillis() % 1000);
        SwingUtilities.invokeLater(() -> timeField.setText(result));
    }
}
