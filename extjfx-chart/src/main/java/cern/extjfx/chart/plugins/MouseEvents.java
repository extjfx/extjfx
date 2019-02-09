/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseButton.SECONDARY;

import javafx.scene.input.MouseEvent;

/**
 * Utility methods for operating on {@link MouseEvent}s.
 */
final class MouseEvents {

    static boolean isOnlyPrimaryButtonDown(MouseEvent event) {
        return event.getButton() == PRIMARY && !event.isMiddleButtonDown() && !event.isSecondaryButtonDown();
    }

    static boolean isOnlySecondaryButtonDown(MouseEvent event) {
        return event.getButton() == SECONDARY && !event.isPrimaryButtonDown() && !event.isMiddleButtonDown();
    }

    static boolean isOnlyCtrlModifierDown(MouseEvent event) {
        return event.isControlDown() && !event.isAltDown() && !event.isMetaDown() && !event.isShiftDown();
    }

    static boolean modifierKeysUp(MouseEvent event) {
        return !event.isAltDown() && !event.isControlDown() && !event.isMetaDown() && !event.isShiftDown();
    }
}
