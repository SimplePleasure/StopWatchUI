import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Processor extends Thread {

    private static final int UPDATE_FREQUENCY_MS = 100;
    private static final String FORMAT_TIME = "%d : %03d";

    private final JLabel timeField;
    private final Lock lock;
    private final Condition startTimerCondition;

    private volatile boolean stopWatchStarted;
    private volatile LocalDateTime timerStarted;

    private Duration period;

    Processor(JLabel timeField) {
        this.timeField = timeField;
        this.stopWatchStarted = false;
        this.lock = new ReentrantLock();
        this.startTimerCondition = lock.newCondition();
    }

    @Override
    public void run() {
        for (;;) {
            lock.lock();
            try {
                while (!stopWatchStarted) {
                    try {
                        startTimerCondition.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                period = Duration.between(timerStarted, LocalDateTime.now());
                String result = String.format(FORMAT_TIME, period.toSeconds(), period.toMillis() % 1000);
                SwingUtilities.invokeLater(() -> timeField.setText(result));

                try {
                    Thread.sleep(UPDATE_FREQUENCY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }

            } finally {
                lock.unlock();
            }
        }
    }

    void startOrResume() {
        lock.lock();
        try {
            if (timerStarted == null) {
                stopWatchStarted = true;
                timerStarted = LocalDateTime.now();
                startTimerCondition.signalAll();
            } else {
                stopWatchStarted = true;
                timerStarted = LocalDateTime.now().minusNanos(period.toNanos());
                startTimerCondition.signalAll();
            }
        } finally {
            lock.unlock();
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
}
