package net.asaken1021.vmmanager.util;

import org.libvirt.DomainInfo.DomainState;

public enum DomainPowerState {
    POWER_RUNNING("Running"),
    POWER_SHUTOFF("Shut off"),
    POWER_OTHER("Other state");

    private String stateText;

    private DomainPowerState(String stateText) {
        this.stateText = stateText;
    }

    public String getStateText() {
        return this.stateText;
    }

    public static DomainPowerState getStateByString(String state) throws InvalidPowerStateException {
        if (state.equals(DomainPowerState.POWER_RUNNING.getStateText())) {
            return DomainPowerState.POWER_RUNNING;
        } else if (state.equals(DomainPowerState.POWER_SHUTOFF.getStateText())) {
            return DomainPowerState.POWER_SHUTOFF;
        }

        throw new InvalidPowerStateException();
    }

    public static DomainPowerState getStateByDomainState(DomainState state){
        if (state.equals(DomainState.VIR_DOMAIN_RUNNING)) {
            return DomainPowerState.POWER_RUNNING;
        } else if (state.equals(DomainState.VIR_DOMAIN_SHUTOFF)) {
            return DomainPowerState.POWER_SHUTOFF;
        } else {
            return DomainPowerState.POWER_OTHER;
        }
    }
}
