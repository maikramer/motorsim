package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import com.billkuker.rocketry.motorsim.Burn;

public interface BurnWatcher {
    void replace(Burn oldBurn, Burn newBurn);
}
