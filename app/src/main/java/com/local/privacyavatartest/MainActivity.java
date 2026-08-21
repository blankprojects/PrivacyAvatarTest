package com.local.privacyavatartest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class MainActivity extends Activity {
    private static final int REQUEST_CONTACTS = 101;
    private static final int REQUEST_CALL_LOG = 102;
    private static final int REQUEST_SMS = 103;
    private static final int REQUEST_CALENDAR = 104;

    private final Map<String, TextView> resultViews = new LinkedHashMap<>();
    private TextView summaryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View content = buildContentView();
        setContentView(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(lightBars, lightBars);
            }
            content.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
        refreshAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (summaryView != null) {
            refreshAll();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshAll();
    }

    private View buildContentView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 247, 246));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text("隐私替身读取测试", 26, Color.rgb(20, 67, 61));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView intro = text(
                "本应用无网络权限，不保存任何读取结果。开启 OPPO 隐私替身后，已授权项目应显示“读取到 0 条”。屏幕样本均已脱敏。",
                15,
                Color.rgb(53, 66, 63));
        intro.setPadding(0, dp(8), 0, dp(14));
        intro.setLineSpacing(0, 1.2f);
        root.addView(intro);

        summaryView = text("", 15, Color.WHITE);
        summaryView.setBackgroundColor(Color.rgb(20, 90, 82));
        summaryView.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(summaryView, matchWidthWrap(dp(8)));

        Button refresh = button("刷新全部读取结果");
        refresh.setOnClickListener(v -> refreshAll());
        root.addView(refresh, matchWidthWrap(dp(12)));

        addTestCard(root, "联系人", Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS);
        addTestCard(root, "通话记录", Manifest.permission.READ_CALL_LOG, REQUEST_CALL_LOG);
        addTestCard(root, "短信", Manifest.permission.READ_SMS, REQUEST_SMS);
        addTestCard(root, "日历", Manifest.permission.READ_CALENDAR, REQUEST_CALENDAR);

        TextView steps = text(
                "建议步骤\n" +
                        "1. 暂时关闭本应用的隐私替身，逐项授权并刷新，确认有数据。\n" +
                        "2. 进入 设置 → 权限与隐私 → 隐私替身，为“隐私替身测试”开启保护。\n" +
                        "3. 返回本应用刷新；对应项目应变为 0 条。\n" +
                        "4. 测试完成后可撤销权限并卸载本应用。",
                14,
                Color.rgb(53, 66, 63));
        steps.setLineSpacing(dp(3), 1.15f);
        steps.setPadding(0, dp(16), 0, 0);
        root.addView(steps);

        return scrollView;
    }

    private void addTestCard(LinearLayout root, String label, String permission, int requestCode) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(14));
        card.setBackgroundColor(Color.WHITE);

        TextView heading = text(label, 19, Color.rgb(20, 67, 61));
        heading.setTypeface(heading.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(heading);

        TextView result = text("尚未检查", 14, Color.rgb(75, 85, 83));
        result.setPadding(0, dp(6), 0, dp(8));
        result.setTextIsSelectable(true);
        resultViews.put(permission, result);
        card.addView(result);

        Button request = button("申请“" + label + "”权限并测试");
        request.setOnClickListener(v -> requestOrRefresh(permission, requestCode));
        card.addView(request, matchWidthWrap(0));
        root.addView(card, matchWidthWrap(dp(10)));
    }

    private void requestOrRefresh(String permission, int requestCode) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            refreshAll();
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    private void refreshAll() {
        int granted = 0;
        int protectedOrEmpty = 0;

        QueryResult contacts = updateResult(
                Manifest.permission.READ_CONTACTS,
                this::queryContacts);
        QueryResult calls = updateResult(
                Manifest.permission.READ_CALL_LOG,
                this::queryCallLog);
        QueryResult sms = updateResult(
                Manifest.permission.READ_SMS,
                this::querySms);
        QueryResult calendar = updateResult(
                Manifest.permission.READ_CALENDAR,
                this::queryCalendar);

        QueryResult[] results = {contacts, calls, sms, calendar};
        for (QueryResult result : results) {
            if (result.permissionGranted) {
                granted++;
                if (result.count == 0) {
                    protectedOrEmpty++;
                }
            }
        }

        if (granted == 0) {
            summaryView.setText("尚未授予任何测试权限");
        } else {
            summaryView.setText(String.format(
                    Locale.getDefault(),
                    "已授权 %d/4 项，其中 %d 项返回空数据",
                    granted,
                    protectedOrEmpty));
        }
    }

    private QueryResult updateResult(String permission, Query query) {
        TextView view = resultViews.get(permission);
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (view != null) {
                view.setText("未授权：点击下方按钮申请权限");
                view.setTextColor(Color.rgb(135, 83, 20));
            }
            return QueryResult.notGranted();
        }

        try {
            QueryResult result = query.run();
            if (view != null) {
                if (result.count == 0) {
                    view.setText("读取到 0 条\n可能已被隐私替身保护，或本机本来没有此类数据。");
                    view.setTextColor(Color.rgb(11, 117, 106));
                } else {
                    view.setText(String.format(
                            Locale.getDefault(),
                            "读取到 %d 条\n脱敏样本：%s",
                            result.count,
                            result.sample));
                    view.setTextColor(Color.rgb(154, 52, 45));
                }
            }
            return result;
        } catch (SecurityException e) {
            if (view != null) {
                view.setText(String.format(Locale.getDefault(), "系统拒绝读取：%s", safeMessage(e)));
                view.setTextColor(Color.rgb(154, 52, 45));
            }
            return QueryResult.error();
        } catch (RuntimeException e) {
            if (view != null) {
                view.setText(String.format(Locale.getDefault(), "读取失败：%s", safeMessage(e)));
                view.setTextColor(Color.rgb(154, 52, 45));
            }
            return QueryResult.error();
        }
    }

    private QueryResult queryContacts() {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        try (Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
            return summarize(cursor, row -> maskName(row.getString(0)) + " / " + maskNumber(row.getString(1)));
        }
    }

    private QueryResult queryCallLog() {
        String[] projection = {
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE
        };
        try (Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC")) {
            return summarize(cursor, row -> {
                String who = TextUtils.isEmpty(row.getString(0)) ? "未命名" : maskName(row.getString(0));
                return who + " / " + maskNumber(row.getString(1)) + " / " + formatDate(row.getLong(2));
            });
        }
    }

    private QueryResult querySms() {
        String[] projection = {
                Telephony.Sms.ADDRESS,
                Telephony.Sms.DATE,
                Telephony.Sms.BODY
        };
        try (Cursor cursor = getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER)) {
            return summarize(cursor, row -> {
                String body = row.getString(2);
                int length = body == null ? 0 : body.length();
                return maskNumber(row.getString(0)) + " / " + formatDate(row.getLong(1)) + " / 正文" + length + "字";
            });
        }
    }

    private QueryResult queryCalendar() {
        String[] projection = {
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART
        };
        try (Cursor cursor = getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                null,
                null,
                CalendarContract.Events.DTSTART + " DESC")) {
            return summarize(cursor, row -> maskName(row.getString(0)) + " / " + formatDate(row.getLong(1)));
        }
    }

    private QueryResult summarize(Cursor cursor, RowFormatter formatter) {
        if (cursor == null) {
            return new QueryResult(true, 0, "无");
        }
        int count = cursor.getCount();
        StringBuilder sample = new StringBuilder();
        int shown = 0;
        while (cursor.moveToNext() && shown < 3) {
            if (shown > 0) {
                sample.append("；");
            }
            sample.append(formatter.format(cursor));
            shown++;
        }
        return new QueryResult(true, count, sample.length() == 0 ? "无" : sample.toString());
    }

    private static String maskName(String value) {
        if (TextUtils.isEmpty(value)) {
            return "空名称";
        }
        int firstEnd = value.offsetByCodePoints(0, 1);
        return value.substring(0, firstEnd) + "***";
    }

    private static String maskNumber(String value) {
        if (TextUtils.isEmpty(value)) {
            return "空号码";
        }
        String compact = value.replaceAll("\\s+", "");
        if (compact.length() <= 4) {
            return "****";
        }
        return "****" + compact.substring(compact.length() - 4);
    }

    private static String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return "无日期";
        }
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(timestamp));
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return TextUtils.isEmpty(message) ? throwable.getClass().getSimpleName() : message;
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        return button;
    }

    private LinearLayout.LayoutParams matchWidthWrap(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface Query {
        QueryResult run();
    }

    private interface RowFormatter {
        String format(Cursor cursor);
    }

    private static final class QueryResult {
        final boolean permissionGranted;
        final int count;
        final String sample;

        QueryResult(boolean permissionGranted, int count, String sample) {
            this.permissionGranted = permissionGranted;
            this.count = count;
            this.sample = sample;
        }

        static QueryResult notGranted() {
            return new QueryResult(false, -1, "");
        }

        static QueryResult error() {
            return new QueryResult(true, -1, "");
        }
    }
}
