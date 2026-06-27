package yuix.tool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiChatActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.fuka.win/v1/chat/completions";
    private static final String API_KEY = "sk-6YN0R82cJtNv4puyz0kbMM2Qp20hg0sLpiKZCARwtSzaI1tw";
    private static final String MODEL = "[鸢尾花]gemini-3.5-flash①";

    private RecyclerView rvMessages;
    private EditText etInput;
    private ImageButton btnSend, btnAttachFile, btnAttachPhoto;
    private ProgressBar progressBar;
    private MaterialCardView btnClear, btnSettings;
    private TextView tvStatus;

    private MessageAdapter adapter;
    private List<Message> messageList;
    private ExecutorService executor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        btnAttachFile = findViewById(R.id.btnAttachFile);
        btnAttachPhoto = findViewById(R.id.btnAttachPhoto);
        progressBar = findViewById(R.id.progressBar);
        btnClear = findViewById(R.id.btnClear);
        btnSettings = findViewById(R.id.btnSettings);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnAttachFile.setOnClickListener(v -> {
            // TODO: Open file picker
            Toast.makeText(this, "选择文件（最大 1GB）", Toast.LENGTH_SHORT).show();
        });

        btnAttachPhoto.setOnClickListener(v -> {
            // TODO: Open photo picker
            Toast.makeText(this, "选择照片", Toast.LENGTH_SHORT).show();
        });

        btnClear.setOnClickListener(v -> showClearConfirmation());

        btnSettings.setOnClickListener(v -> {
            // TODO: Open AI settings
            Toast.makeText(this, "AI 设置", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) return;

        Message userMessage = new Message(input, Message.TYPE_USER);
        messageList.add(userMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etInput.setText("");

        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("AI 正在思考...");

        executor.execute(() -> {
            try {
                String response = callAiApi(input);
                mainHandler.post(() -> {
                    Message aiMessage = new Message(response, Message.TYPE_AI);
                    messageList.add(aiMessage);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvMessages.scrollToPosition(messageList.size() - 1);
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("就绪");
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Message errorMessage = new Message("错误: " + e.getMessage(), Message.TYPE_AI);
                    messageList.add(errorMessage);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("错误");
                });
            }
        });
    }

    private String callAiApi(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();
        for (Message msg : messageList) {
            JSONObject msgObj = new JSONObject();
            msgObj.put("role", msg.type == Message.TYPE_USER ? "user" : "assistant");
            msgObj.put("content", msg.content);
            messages.put(msgObj);
        }
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        OutputStream os = conn.getOutputStream();
        os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject msg = choice.getJSONObject("message");
                return msg.getString("content");
            }
            return "无响应";
        } else {
            return "API 错误: " + responseCode;
        }
    }

    private void showClearConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("清除 AI 记忆")
            .setMessage("清空后 AI 会失去所有记忆，确定要继续吗？\n\nClearing will make AI lose all memory. Continue?")
            .setPositiveButton("确定", (dialog, which) -> {
                messageList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "已清除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void loadMessages() {
        // TODO: Load messages from zip storage
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    static class Message {
        static final int TYPE_USER = 0;
        static final int TYPE_AI = 1;

        String content;
        int type;

        Message(String content, int type) {
            this.content = content;
            this.type = type;
        }
    }
}
