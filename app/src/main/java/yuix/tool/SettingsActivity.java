package yuix.tool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardClearMemory, cardColorTheme, cardApiConfig;
    private TextView tvApiUrl, tvModel, tvApiKey;

    private static final String PREFS_NAME = "yuix_tool_prefs";
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_MODEL = "model";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_BG_COLOR = "bg_color";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupClickListeners();
        loadSettings();
    }

    private void initViews() {
        cardClearMemory = findViewById(R.id.cardClearMemory);
        cardColorTheme = findViewById(R.id.cardColorTheme);
        cardApiConfig = findViewById(R.id.cardApiConfig);
        tvApiUrl = findViewById(R.id.tvApiUrl);
        tvModel = findViewById(R.id.tvModel);
        tvApiKey = findViewById(R.id.tvApiKey);
    }

    private void setupClickListeners() {
        cardClearMemory.setOnClickListener(v -> showClearMemoryConfirmation());

        cardColorTheme.setOnClickListener(v -> showColorPicker());

        cardApiConfig.setOnClickListener(v -> showApiConfigDialog());
    }

    private void showClearMemoryConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("清除 AI 记忆")
            .setMessage("清空后 AI 会失去所有记忆，包括：\n" +
                "- 所有对话记录\n" +
                "- 学习数据\n" +
                "- 用户偏好\n\n" +
                "确定要继续吗？\n\n" +
                "Clearing will make AI lose all memory. Continue?")
            .setPositiveButton("确定", (dialog, which) -> {
                clearAiMemory();
                Toast.makeText(this, "已清除 AI 记忆", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void clearAiMemory() {
        // Clear conversation history
        // Clear learning data
        // Clear user preferences
        prefs.edit().clear().apply();
    }

    private void showColorPicker() {
        String[] colors = {"默认蓝", "紫色", "绿色", "橙色", "粉色"};
        new AlertDialog.Builder(this)
            .setTitle("选择主题颜色")
            .setItems(colors, (dialog, which) -> {
                String[] colorValues = {"#60A5FA", "#A78BFA", "#4ADE80", "#FBBF24", "#F472B6"};
                saveThemeColor(colorValues[which]);
                Toast.makeText(this, "主题颜色已更改", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void showApiConfigDialog() {
        // Show dialog to edit API configuration
        new AlertDialog.Builder(this)
            .setTitle("API 配置")
            .setMessage("当前配置:\n" +
                "API URL: https://api.fuka.win/v1\n" +
                "Model: [鸢尾花]gemini-3.5-flash①\n" +
                "API Key: sk-***...***")
            .setPositiveButton("确定", null)
            .show();
    }

    private void saveThemeColor(String color) {
        prefs.edit().putString(KEY_THEME_COLOR, color).apply();
    }

    private void loadSettings() {
        String apiUrl = prefs.getString(KEY_API_URL, "https://api.fuka.win/v1");
        String model = prefs.getString(KEY_MODEL, "[鸢尾花]gemini-3.5-flash①");
        String apiKey = prefs.getString(KEY_API_KEY, "sk-6YN0R82cJtNv4puyz0kbMM2Qp20hg0sLpiKZCARwtSzaI1tw");

        tvApiUrl.setText(apiUrl);
        tvModel.setText(model);
        tvApiKey.setText(apiKey.substring(0, 10) + "***...***");
    }
}
