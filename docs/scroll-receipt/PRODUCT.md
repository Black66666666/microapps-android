# Scroll Receipt — product source of truth

Scroll Receipt is a free Android application that measures how many short vertical videos a person actually watches in TikTok, Instagram Reels and YouTube Shorts, how much active time that consumes, then turns the result into a recognizable shareable receipt. The first version has no ads, subscription, premium tier, registration, mandatory account or in-app donation.

The product loop is: usage → surprising result → receipt → share → another person wants their own result → install.

## Current platform decision

iOS is excluded from the first product because the approved public iOS mechanisms do not provide functional parity for counting individual short-video transitions. Do not substitute an estimate based on screen time.

## Gate 0A

Before production MVP work, Android must prove real transition counting for TikTok, Reels and Shorts on physical devices. Target error is <=5% per platform, with no systematic double counting or mass misses. Time must stop outside recognized short-video mode, in background and with screen off.

Normal user measurement must be automatic after Accessibility is enabled. Start buttons belong only to controlled accuracy diagnostics.

## Privacy boundary

Gate 0A uses structural Accessibility information: package, event type, resource IDs, widget classes, indices and scroll geometry. It must not collect messages, typed text, captions, comments, usernames, screenshots, images, audio, video, contacts, clipboard, exact location or browser history. Gate 0A has no network permission.

## Visual language

Use the repository shared `core/designsystem`: dark navy/violet background, glass cards, cyan/purple/pink accents, one dominant metric and minimal clutter.
