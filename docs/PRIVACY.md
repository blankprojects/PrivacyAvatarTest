# Privacy design

Privacy Avatar Test is intentionally designed as a local observation tool rather than a general contacts or diagnostics application.

## Data accessed

Only data sources explicitly selected by the user are queried: contacts, call logs, SMS, and calendar events. The app requests each runtime permission separately.

## Data displayed

The app displays a record count and at most three masked samples. Names keep only their first Unicode code point. Phone numbers keep only their last four characters. SMS bodies are never displayed; only their character counts are shown.

## Data not collected

The app does not persist query results, write files, use a database, copy values to the clipboard, or intentionally write sensitive values to logs. Android backup and device-transfer extraction are disabled.

## Network boundary

The application manifest does not request `android.permission.INTERNET`, so the Android application sandbox does not allow the app to create network sockets. CI rejects any source change that introduces this permission.

This guarantee applies to builds produced from the reviewed source. A modified third-party APK can behave differently; build from source or verify the APK manifest when provenance matters.

## Interpretation limits

A zero-row query is consistent with Privacy Avatar protection but is not proof by itself. Empty providers, OS restrictions, unsupported permissions, profile isolation, or provider errors can produce the same result. Use a before/after comparison on the same device and data set.
