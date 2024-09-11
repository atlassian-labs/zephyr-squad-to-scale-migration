package atlassian.migration.app.zephyr.common;

import java.util.concurrent.TimeUnit;

public class ProgressBarUtil {

    private static final Integer TOTAL_BARS = 30;

    public static String getProgressBar(int currentIndex, int total, long startTimeMillis) {

        var elapsedMillis = System.currentTimeMillis() - startTimeMillis;

        var hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
        elapsedMillis -= TimeUnit.HOURS.toMillis(hours);
        var minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
        elapsedMillis -= TimeUnit.MINUTES.toMillis(minutes);
        var seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);

        if (total == 0) {
            return String.format("  0%% [%s>%s] %d/%d (Elapsed Time: %02d:%02d:%02d)",
                    "=".repeat(0), " ".repeat(TOTAL_BARS), currentIndex, total, hours, minutes, seconds);
        }

        var progress = (double) currentIndex / total;
        var percentage = progress * 100;

        var roundedPercentage = Math.round(percentage * 100.0) / 100.0;
        var completedBars = (int) (progress * TOTAL_BARS);
        var remainingBars = TOTAL_BARS - completedBars;

        return String.format("%3.0f%% [%s>%s] %d/%d (Elapsed Time: %02d:%02d:%02d)",
                roundedPercentage, "=".repeat(completedBars), " ".repeat(remainingBars), currentIndex, total, hours, minutes, seconds);
    }
}