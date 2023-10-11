import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Processor extends Thread {

    private final JLabel timeField;
    private final Lock lock;
    private final Condition startTimerCondition;

    private volatile boolean stopWatchStarted;
    private volatile LocalDateTime timerStarted;

    private Duration period;

    Processor(JLabel timeField) {
        this.stopWatchStarted = false;
        this.timeField = timeField;
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
                String result = String.format("%d : %03d", period.toSeconds(), period.toMillis() % 1000);
                SwingUtilities.invokeLater(() -> timeField.setText(result));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
