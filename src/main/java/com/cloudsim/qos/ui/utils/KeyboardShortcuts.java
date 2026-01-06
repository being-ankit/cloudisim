package com.cloudsim.qos.ui.utils;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages keyboard shortcuts for the application
 */
public class KeyboardShortcuts {
    
    private final Map<KeyCombination, ShortcutAction> shortcuts = new HashMap<>();
    private Scene scene;
    
    public KeyboardShortcuts(Scene scene) {
        this.scene = scene;
        registerDefaultShortcuts();
        setupKeyHandler();
    }
    
    private void registerDefaultShortcuts() {
        // File operations
        register(KeyCode.N, KeyCombination.CONTROL_DOWN, "New Configuration", null);
        register(KeyCode.O, KeyCombination.CONTROL_DOWN, "Open Configuration", null);
        register(KeyCode.S, KeyCombination.CONTROL_DOWN, "Save Configuration", null);
        register(KeyCode.E, KeyCombination.CONTROL_DOWN, "Export Results", null);
        
        // Simulation
        register(KeyCode.R, KeyCombination.CONTROL_DOWN, "Run Simulation", null);
        register(KeyCode.ESCAPE, "Stop Simulation", null);
        
        // Navigation
        register(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN, "Go to Welcome", null);
        register(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN, "Go to Configuration", null);
        register(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN, "Go to Tasks", null);
        register(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN, "Go to VMs", null);
        register(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN, "Go to Simulation", null);
        register(KeyCode.DIGIT6, KeyCombination.CONTROL_DOWN, "Go to Results", null);
        register(KeyCode.DIGIT7, KeyCombination.CONTROL_DOWN, "Go to Charts", null);
        register(KeyCode.DIGIT8, KeyCombination.CONTROL_DOWN, "Go to Reports", null);
        register(KeyCode.DIGIT9, KeyCombination.CONTROL_DOWN, "Go to Comparison", null);
        
        // View
        register(KeyCode.F5, "Refresh", null);
        register(KeyCode.F11, "Toggle Fullscreen", null);
        
        // Edit
        register(KeyCode.Z, KeyCombination.CONTROL_DOWN, "Undo", null);
        register(KeyCode.Y, KeyCombination.CONTROL_DOWN, "Redo", null);
        
        // Settings
        register(KeyCode.COMMA, KeyCombination.CONTROL_DOWN, "Open Settings", null);
        
        // Quit
        register(KeyCode.Q, KeyCombination.CONTROL_DOWN, "Quit", null);
        
        // Help
        register(KeyCode.F1, "Help", null);
    }
    
    private void setupKeyHandler() {
        scene.setOnKeyPressed(event -> {
            for (Map.Entry<KeyCombination, ShortcutAction> entry : shortcuts.entrySet()) {
                if (entry.getKey().match(event)) {
                    ShortcutAction action = entry.getValue();
                    if (action.handler != null) {
                        action.handler.run();
                        event.consume();
                    }
                    break;
                }
            }
        });
    }
    
    public void register(KeyCode key, String description, Runnable handler) {
        KeyCombination combo = new KeyCodeCombination(key);
        shortcuts.put(combo, new ShortcutAction(description, handler));
    }
    
    public void register(KeyCode key, KeyCombination.Modifier modifier, String description, Runnable handler) {
        KeyCombination combo = new KeyCodeCombination(key, modifier);
        shortcuts.put(combo, new ShortcutAction(description, handler));
    }
    
    public void setHandler(KeyCode key, Runnable handler) {
        KeyCombination combo = new KeyCodeCombination(key);
        if (shortcuts.containsKey(combo)) {
            shortcuts.get(combo).handler = handler;
        }
    }
    
    public void setHandler(KeyCode key, KeyCombination.Modifier modifier, Runnable handler) {
        KeyCombination combo = new KeyCodeCombination(key, modifier);
        if (shortcuts.containsKey(combo)) {
            shortcuts.get(combo).handler = handler;
        }
    }
    
    public Map<String, String> getShortcutDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        for (Map.Entry<KeyCombination, ShortcutAction> entry : shortcuts.entrySet()) {
            descriptions.put(entry.getKey().getDisplayText(), entry.getValue().description);
        }
        return descriptions;
    }
    
    private static class ShortcutAction {
        String description;
        Runnable handler;
        
        ShortcutAction(String description, Runnable handler) {
            this.description = description;
            this.handler = handler;
        }
    }
}
