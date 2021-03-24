package org.cloudbus.cloudsim.schedulers.vm;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VmSchedulerSpaceSharedTest {
    private static final double MIPS = 1000;
    private static final int VM_PES_NUMBER = 2;
    private VmScheduler vmScheduler;

    private VmScheduler createVmScheduler(final double mips, final int pesNumber) {
        final VmSchedulerSpaceShared scheduler = new VmSchedulerSpaceShared();
        final List<Pe> peList = new ArrayList<>(pesNumber);
        LongStream.range(0, pesNumber).forEach(i -> peList.add(new PeSimple(mips, new PeProvisionerSimple())));
        final Host host = new HostSimple(2048, 20000, 20000, peList);
        host
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(scheduler)
            .setId(0);
        return scheduler;
    }

    @BeforeEach
    public void setUp() {
        vmScheduler = createVmScheduler(MIPS,  VM_PES_NUMBER);
    }

    @Test
    public void testIsSuitableForVmWhenAllPesAreTakenAndThereIsEnoughMips() {
        final Vm vm0 = VmTestUtil.createVm(0, MIPS / 2, 2);
        final List<Double> mipsShare1 = new ArrayList<>(1);
        mipsShare1.add(MIPS / 2);
        mipsShare1.add(MIPS / 2);

        assertTrue(vmScheduler.allocatePesForVm(vm0, mipsShare1));
        assertEquals(MIPS, vmScheduler.getTotalAvailableMips());
        assertEquals(MIPS, vmScheduler.getTotalAllocatedMipsForVm(vm0));

        final Vm vm1 = VmTestUtil.createVm(1, MIPS, 2);
        assertTrue(vmScheduler.isSuitableForVm(vm1));

        vmScheduler.deallocatePesForAllVms();
        assertEquals(MIPS * 2, vmScheduler.getTotalAvailableMips());
    }

    @Test
    public void testIsNotSuitableForVmWhenAllPesAreTakenAndThereIsNotEnoughMips() {
        final Vm vm0 = VmTestUtil.createVm(0, MIPS, 2);
        final List<Double> mipsShare1 = new ArrayList<>(2);
        mipsShare1.add(MIPS);
        mipsShare1.add(MIPS / 2);

        assertTrue(vmScheduler.allocatePesForVm(vm0, mipsShare1));
        assertEquals(MIPS / 2, vmScheduler.getTotalAvailableMips());
        assertEquals(3 * MIPS / 2, vmScheduler.getTotalAllocatedMipsForVm(vm0));

        final Vm vm1 = VmTestUtil.createVm(1, MIPS * 2, 2);
        assertFalse(vmScheduler.isSuitableForVm(vm1));

        vmScheduler.deallocatePesForAllVms();
        assertEquals(MIPS * 2, vmScheduler.getTotalAvailableMips());
    }
}
