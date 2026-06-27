package yuix.tool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerminalActivity extends AppCompatActivity {

    private TextView tvOutput;
    private EditText etInput;
    private ScrollView svOutput;
    private MaterialCardView btnClear;

    private ExecutorService executor;
    private Handler mainHandler;
    private StringBuilder outputBuffer = new StringBuilder();

    private String workingDir = "/data/data/yuix.tool/files/linux";
    private Process shellProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupTerminal();
        setupClickListeners();
    }

    private void initViews() {
        tvOutput = findViewById(R.id.tvOutput);
        etInput = findViewById(R.id.etInput);
        svOutput = findViewById(R.id.svOutput);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setupTerminal() {
        // Create working directory
        File workDir = new File(workingDir);
        if (!workDir.exists()) {
            workDir.mkdirs();
        }

        appendOutput("yuix.tool Linux Container\n");
        appendOutput("Working directory: " + workingDir + "\n");
        appendOutput("Type 'help' for available commands\n\n");
    }

    private void setupClickListeners() {
        etInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                String command = etInput.getText().toString().trim();
                if (!command.isEmpty()) {
                    executeCommand(command);
                    etInput.setText("");
                }
                return true;
            }
            return false;
        });

        btnClear.setOnClickListener(v -> {
            outputBuffer.setLength(0);
            tvOutput.setText("");
            appendOutput("Terminal cleared\n\n");
        });
    }

    private void executeCommand(String command) {
        appendOutput("$ " + command + "\n");

        if (command.equals("help")) {
            showHelp();
            return;
        }

        if (command.equals("clear")) {
            outputBuffer.setLength(0);
            tvOutput.setText("");
            return;
        }

        executor.execute(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
                pb.directory(new File(workingDir));
                pb.redirectErrorStream(true);

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    output.append("[Exit code: ").append(exitCode).append("]\n");
                }

                mainHandler.post(() -> {
                    appendOutput(output.toString());
                    appendOutput("\n");
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    appendOutput("Error: " + e.getMessage() + "\n\n");
                });
            }
        });
    }

    private void showHelp() {
        String help = "Available commands:\n" +
            "  help     - Show this help\n" +
            "  clear    - Clear terminal\n" +
            "  ls       - List files\n" +
            "  cd       - Change directory\n" +
            "  pwd      - Print working directory\n" +
            "  cat      - Display file contents\n" +
            "  echo     - Print text\n" +
            "  mkdir    - Create directory\n" +
            "  touch    - Create file\n" +
            "  rm       - Remove file/directory\n" +
            "  cp       - Copy file\n" +
            "  mv       - Move file\n" +
            "  ...      - And other standard Linux commands\n\n";
        appendOutput(help);
    }

    private void appendOutput(String text) {
        outputBuffer.append(text);
        tvOutput.setText(outputBuffer.toString());
        svOutput.post(() -> svOutput.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (shellProcess != null) {
            shellProcess.destroy();
        }
    }
}
