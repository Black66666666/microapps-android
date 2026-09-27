# Scroll Receipt Gate 0A — physical-device test plan

Run independent series for TikTok, Instagram Reels and YouTube Shorts.

## Baseline series

1. Enable the Scroll Receipt Accessibility service once.
2. Open Accuracy diagnostics and choose the target app test. This does not enable normal counting; it only stores the test baseline and opens the target app.
3. Enter the target short-video mode.
4. Count the first visible video as 1.
5. Perform exactly 100 transitions to new videos.
6. Manual reference = 101 visible videos.
7. Return to Scroll Receipt, enter 101 and finish the test.
8. Export the Gate 0A JSON report.

Repeat several independent series, on several physical Android devices and Android versions where possible.

## Pass criteria

- counter error <=5% on each supported platform;
- no systematic double counting;
- no mass misses;
- comments/profile overlays do not count as new videos;
- leaving short-video mode stops active timing;
- app background and screen lock stop active timing;
- closing the Scroll Receipt activity does not stop automatic measurement while Accessibility remains enabled.

Do not close Gate 0A on an average result if one platform systematically fails.
