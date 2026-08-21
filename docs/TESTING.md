# Testing guide

## Recommended baseline test

Use synthetic contacts whenever possible.

1. Create one clearly identifiable synthetic contact, such as `Privacy Test 001` with a non-real number.
2. Disable Privacy Avatar protection for this app.
3. Grant only `READ_CONTACTS` from inside the app.
4. Refresh and confirm that the contacts result is non-zero.
5. Enable contact protection for this app in ColorOS Privacy Avatar settings.
6. Return to the app and refresh again.
7. A protected query is expected to return zero rows.
8. Disable protection once more and verify that the baseline returns, then remove the synthetic contact.

This A/B/A sequence distinguishes protection from an empty provider or an unrelated permission problem.

## Other data sources

`READ_SMS` and `READ_CALL_LOG` are restricted permissions on modern Android. A device or installer may refuse to grant them to a regular sideloaded app. Treat that as an OS restriction, not a failed Privacy Avatar test.

## Post-test cleanup

Revoke all four permissions, remove any synthetic records, and uninstall the application. The app does not retain its own copy of queried records.
