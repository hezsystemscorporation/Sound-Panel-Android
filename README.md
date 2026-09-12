# Sound-Panel-Android

Main is corresponding to AC version and the 4.1.2 apk package. 

**&copy;2026 Michael Hertz (HEZ Group). Attribution is strictly required, Any reuse, modification, or derivative work must explicitly credit the original author. **

**&copy;2026 何昕 (HEZ Group). 署名要求：任何形式的重用、修改或基於此項目的二次衍生，均必須明確標示原作者署名。**

## Introduction

This is a software and hardware interactive central control system based on native Android development. The system supports importing configuration files to dynamically generate the operating interface. It features a built-in professional multi-channel audio mixer (supporting various playback modes such as primary/secondary audio and background music, alongside independent control and mixing of custom voice tracks).

Via Type-C OTG, it connects directly to the underlying serial port driver, allowing the system to precisely send structured control signals to the ESP32 at the exact moment a specific step is executed. Combined with a real-time decibel meter, non-linear loop control, a gesture-based emergency anti-mistouch mechanism, four-language internationalization, and a custom persistent theme engine with contrast detection, this application provides a zero-latency, fully controlled, and highly polished on-site operational solution for complex scenarios such as offline hardware demonstrations and interactive art installations.

## Changelog

**v1.0.3**

* **Initial Release!** Added basic configuration file import and control features.
* Supports sequential playback of the action library.
* Sequence control via "Previous", "Next", "First Step", "Last Step", and the central main button.
* Sub-audio support for playing or pausing music and other voice tracks in specific scenes.
* Added a dropdown menu via the top button for quick jumping to designated actions.

**v1.1.2**

* Added round recording and display functionality.
* Fixed known issues and comprehensively improved the UI and user experience.

**v1.2.1-beta**

* Added global audio functionality: supports reading global audio from the config file and importing it to the interface, allowing users to manually trigger playback at any time during the action flow.

**v1.2.3-beta**

* The home page now displays the time added and the configuration file name.
* Added support for directly deleting unwanted configuration files.

**v1.3.2-beta**

* Added custom start and end point settings, allowing users to flexibly skip unwanted actions.

**v2.0.1-beta**

* Full multi-language support in conjunction with the sequence editor. Users can instantly switch languages via the top-right button.
* *Compatibility Note:* Old configuration files are no longer supported starting from this version. Please use new configuration files containing the `languages` field.

**v2.1.8a**

* **Console Feature:** Supports adding Background Music (BGM) with independent playback.
* **Volume Control & Monitoring:** Supports separate volume adjustments for main audio and BGM, with real-time audio monitoring via a decibel meter.
* **Loop Integration:** Integrated the custom loop area functionality into the console.
* **Notched Screen Adaptation:** Interface optimized for devices with notches or Dynamic Islands, proactively reserving a top safe area to prevent screen occlusion.

**v2.3.2c**

* UI optimization: Changed the console to an independent layer to free up screen space, delivering a smoother operating experience.

**v2.4.1**

* **BGM Upgrade:** Now supports adding multiple tracks. Defaults to list-loop mode, and supports single-loop and random playback.

**v2.5.3**

* Multi-language UI support (currently supports Traditional Chinese, English, and Japanese).
* Custom UI color theme replacement.
* **Accessibility Enhancements:** Provided a high-contrast mode for visually impaired users, and added accessibility tags to support voice navigation and screen readers.

**v3.0.1**

* Added support for transmitting specific data formats to the ESP32 transmitter, enabling the receiving device to instantly display the triggered action.

**v4.0.3b**

* **Personalized Theme Colors:** Supports custom UI colors via RGB sliders; the system will automatically issue a warning if insufficient color contrast affects text readability.
* **Global Audio Upgrade:** Supports adding items to global audio with persistent storage and deletion functions. Added "long-press and drag" for quick playback sorting.
* **Top Navigation Bar:** When enabled, an extra set of operation buttons loads at the top of the screen, simultaneously increasing button spacing in the core control area for easier one-handed use or blind operation.
* **Independent Branch Disabling:** For actions with multiple branches, clicking the "eye icon" at the top right of the action card enables or disables that branch. Once disabled, clicking the item will not trigger any action.
* **Gesture Anti-Mistouch Cancellation:** Added a swipe-to-cancel gesture. If a button is pressed by mistake, swiftly moving your finger outside the trigger area before the audio plays will instantly release the trigger state to prevent misoperation.
* **BGM Playback Expansion:** Added "Single Play" mode (stops automatically after one track, neither repeating nor skipping to the next).

**v4.0.4**

* Fixed multi-language display issues.
* Further optimized the UI for a better overall operating experience.
* Added an "About" interface.

**v4.0.6**

* Updated and optimized the UI and related operations.
* Updated support for Virtual Machine (VM) devices.

**v4.0.7**

* Updated and optimized the UI and interactive experience.

**v4.0.8**

* Added custom interactive mode options, giving users more autonomy and customization space.
* Added a ZIP file validation mechanism and large-file progress prompts.
* Fixed scrolling conflict issues in the BGM list.

**v4.1.1**

* **Global Audio Validation:** Adding a global audio track that shares a name with an existing track in the package or a previously added track will now be blocked.
* **Sequence Protection:** Prevented the disabling of all sequence steps; a prompt will appear if attempted.
* **Quick Restore:** Added a "Restore all sequences" function on the right side of the quick jump dropdown menu.
* UI and UX optimizations.
* **Large File Prompt:** Automatically displays an asynchronous loading prompt and spinner when importing ZIP files larger than 20MB.
* **Strict Format Validation:** Non-standard ZIPs or other formats are intercepted with a prompt to import the correct ZIP package (packages generated by the old PC client are no longer supported).
* **Pre-read Integrity Check:** Scans the file structure before unpacking. If corrupted or missing files are detected, it automatically clears the residual cache and prompts the user to re-import.
* **Scroll Conflict Fix:** Fixed the issue in the console where multiple imported BGMs could only be dragged to sort, preventing downward scrolling of the list.

**v4.1.2**

* **File Validation Upgrade:** Added internal file validation. If files declared within the package do not exist, the package is flagged as incomplete, the cache is automatically cleared, and a re-import prompt is triggered.
