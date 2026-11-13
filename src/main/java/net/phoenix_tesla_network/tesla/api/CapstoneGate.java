package net.phoenix_tesla_network.tesla.api;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import java.util.function.Predicate;

/**
 * A hook for a pack to decide whether the Tesla Tower may run. The Tesla Tower is one of the capstone multiblocks that
 * the Phoenix Forge's rebirth network can hold back until it is linked. This mod does not know about that network, so
 * it only asks here: by default the answer is always yes, and whatever installs a different check owns the rule.
 */
public final class CapstoneGate {

    private CapstoneGate() {}

    public static volatile Predicate<MetaMachine> check = machine -> true;

    public static boolean allows(MetaMachine machine) {
        try {
            return check.test(machine);
        } catch (RuntimeException e) {
            return true;
        }
    }
}
