/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.simstatus;

import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .CELL_DATA_NETWORK_TYPE_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .CELL_VOICE_NETWORK_TYPE_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController.EID_INFO_LABEL_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController.EID_INFO_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .ICCID_INFO_LABEL_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .ICCID_INFO_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .IMS_REGISTRATION_STATE_LABEL_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .IMS_REGISTRATION_STATE_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .NETWORK_PROVIDER_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .OPERATOR_INFO_LABEL_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .OPERATOR_INFO_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .PHONE_NUMBER_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .ROAMING_INFO_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .SERVICE_STATE_VALUE_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .SIGNAL_STRENGTH_LABEL_ID;
import static com.android.settings.deviceinfo.simstatus.SimStatusDialogController
        .SIGNAL_STRENGTH_VALUE_ID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccCardInfo;
import android.telephony.euicc.EuiccManager;

import androidx.lifecycle.LifecycleOwner;

import com.android.internal.telephony.PhoneConstants;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class SimStatusDialogControllerTest {

    @Mock
    private SimStatusDialogFragment mDialog;
    @Mock
    private TelephonyManager mTelephonyManager;
    @Mock
    private SubscriptionInfo mSubscriptionInfo;
    @Mock
    private ServiceState mServiceState;
    @Mock
    private PhoneStateListener mPhoneStateListener;
    @Mock
    private SignalStrength mSignalStrength;
    @Mock
    private CarrierConfigManager mCarrierConfigManager;
    @Mock
    private PersistableBundle mPersistableBundle;
    @Mock
    private EuiccManager mEuiccManager;
    @Mock
    private SubscriptionManager mSubscriptionManager;

    private SimStatusDialogController mController;
    private Context mContext;
    private LifecycleOwner mLifecycleOwner;
    private Lifecycle mLifecycle;

    private static final String TEST_EID_FROM_CARD = "11111111111111111111111111111111";
    private static final String TEST_EID_FROM_MANAGER = "22222222222222222222222222222222";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;
        when(mDialog.getContext()).thenReturn(mContext);
        mLifecycleOwner = () -> mLifecycle;
        mLifecycle = new Lifecycle(mLifecycleOwner);
        mController = spy(new SimStatusDialogController(mDialog, mLifecycle, 0 /* phone id */));
        doReturn(mServiceState).when(mController).getCurrentServiceState();
        doReturn(0).when(mSignalStrength).getDbm();
        doReturn(0).when(mSignalStrength).getAsuLevel();
        doReturn(mPhoneStateListener).when(mController).getPhoneStateListener();
        doReturn("").when(mController).getPhoneNumber();
        doReturn(mSignalStrength).when(mController).getSignalStrength();
        doReturn(mSubscriptionInfo).when(mSubscriptionManager).getActiveSubscriptionInfo(anyInt());

        ReflectionHelpers.setField(mController, "mTelephonyManager", mTelephonyManager);
        ReflectionHelpers.setField(mController, "mCarrierConfigManager", mCarrierConfigManager);
        ReflectionHelpers.setField(mController, "mSubscriptionInfo", mSubscriptionInfo);
        ReflectionHelpers.setField(mController, "mEuiccManager", mEuiccManager);
        ReflectionHelpers.setField(mController, "mSubscriptionManager", mSubscriptionManager);

        when(mTelephonyManager.getPhoneCount()).thenReturn(
                PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(new ArrayList<UiccCardInfo>());
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(
                new HashMap<Integer, Integer>());

        when(mEuiccManager.isEnabled()).thenReturn(false);
        when(mEuiccManager.getEid()).thenReturn("");
        when(mEuiccManager.createForCardId(anyInt())).thenReturn(mEuiccManager);

        when(mCarrierConfigManager.getConfigForSubId(anyInt())).thenReturn(mPersistableBundle);
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL))
                .thenReturn(true);

        final ShadowPackageManager shadowPackageManager =
            Shadows.shadowOf(RuntimeEnvironment.application.getPackageManager());
        final PackageInfo sysUIPackageInfo = new PackageInfo();
        sysUIPackageInfo.packageName = "com.android.systemui";
        shadowPackageManager.addPackage(sysUIPackageInfo);
    }

    @Test
    public void initialize_updateNetworkProviderWithFoobarCarrier_shouldUpdateCarrierWithFoobar() {
        final CharSequence carrierName = "foobar";
        doReturn(carrierName).when(mSubscriptionInfo).getCarrierName();

        mController.initialize();

        verify(mDialog).setText(NETWORK_PROVIDER_VALUE_ID, carrierName);
    }

    @Test
    public void initialize_updatePhoneNumberWith1111111111_shouldUpdatePhoneNumber() {
        final String phoneNumber = "1111111111";
        doReturn(phoneNumber).when(mController).getPhoneNumber();

        mController.initialize();

        verify(mDialog).setText(PHONE_NUMBER_VALUE_ID, phoneNumber);
    }

    @Test
    public void initialize_updateLatestAreaInfoWithCdmaPhone_shouldRemoveOperatorInfoSetting() {
        when(mTelephonyManager.getPhoneType()).thenReturn(TelephonyManager.PHONE_TYPE_CDMA);

        mController.initialize();

        verify(mDialog).removeSettingFromScreen(OPERATOR_INFO_LABEL_ID);
        verify(mDialog).removeSettingFromScreen(OPERATOR_INFO_VALUE_ID);
    }

    @Test
    public void initialize_updateServiceStateWithInService_shouldUpdateTextToBeCInService() {
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);

        mController.initialize();

        final String inServiceText = mContext.getString(R.string.radioInfo_service_in);
        verify(mDialog).setText(SERVICE_STATE_VALUE_ID, inServiceText);
    }

    @Test
    public void initialize_updateServiceStateWithPowerOff_shouldUpdateTextAndResetSignalStrength() {
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_POWER_OFF);
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        final String offServiceText = mContext.getString(R.string.radioInfo_service_off);
        verify(mDialog).setText(SERVICE_STATE_VALUE_ID, offServiceText);
        verify(mDialog).setText(SIGNAL_STRENGTH_VALUE_ID, "0");
    }

    @Test
    public void initialize_updateVoiceDataOutOfService_shouldUpdateSettingAndResetSignalStrength() {
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);
        when(mServiceState.getDataRegState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        final String offServiceText = mContext.getString(R.string.radioInfo_service_out);
        verify(mDialog).setText(SERVICE_STATE_VALUE_ID, offServiceText);
        verify(mDialog).setText(SIGNAL_STRENGTH_VALUE_ID, "0");
    }

    @Test
    public void initialize_updateVoiceOutOfServiceDataInService_shouldUpdateTextToBeInService() {
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);
        when(mServiceState.getDataRegState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        final String inServiceText = mContext.getString(R.string.radioInfo_service_in);
        verify(mDialog).setText(SERVICE_STATE_VALUE_ID, inServiceText);
    }

    @Test
    public void initialize_updateSignalStrengthWith50_shouldUpdateSignalStrengthTo50() {
        final int signalDbm = 50;
        final int signalAsu = 50;
        doReturn(signalDbm).when(mSignalStrength).getDbm();
        doReturn(signalAsu).when(mSignalStrength).getAsuLevel();
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        final String signalStrengthString =
            mContext.getString(R.string.sim_signal_strength, signalDbm, signalAsu);
        verify(mDialog).setText(SIGNAL_STRENGTH_VALUE_ID, signalStrengthString);
    }

    @Test
    public void initialize_updateVoiceOutOfServiceDataInService_shouldUpdateSignalStrengthTo50() {
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);
        when(mServiceState.getDataRegState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        final int signalDbm = 50;
        final int signalAsu = 50;
        doReturn(signalDbm).when(mSignalStrength).getDbm();
        doReturn(signalAsu).when(mSignalStrength).getAsuLevel();
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        final String signalStrengthString =
                mContext.getString(R.string.sim_signal_strength, signalDbm, signalAsu);
        verify(mDialog).setText(SIGNAL_STRENGTH_VALUE_ID, signalStrengthString);
    }

    @Test
    public void initialize_updateVoiceNetworkTypeWithEdge_shouldUpdateSettingToEdge() {
        when(mTelephonyManager.getVoiceNetworkType(anyInt())).thenReturn(
                TelephonyManager.NETWORK_TYPE_EDGE);

        mController.initialize();

        verify(mDialog).setText(CELL_VOICE_NETWORK_TYPE_VALUE_ID,
                TelephonyManager.getNetworkTypeName(TelephonyManager.NETWORK_TYPE_EDGE));
    }

    @Test
    public void initialize_updateDataNetworkTypeWithEdge_shouldUpdateSettingToEdge() {
        when(mTelephonyManager.getDataNetworkType(anyInt())).thenReturn(
                TelephonyManager.NETWORK_TYPE_EDGE);

        mController.initialize();

        verify(mDialog).setText(CELL_DATA_NETWORK_TYPE_VALUE_ID,
                TelephonyManager.getNetworkTypeName(TelephonyManager.NETWORK_TYPE_EDGE));
    }

    @Test
    public void initialize_updateRoamingStatusIsRoaming_shouldSetSettingToRoaming() {
        when(mServiceState.getRoaming()).thenReturn(true);

        mController.initialize();

        final String roamingOnString = mContext.getString(R.string.radioInfo_roaming_in);
        verify(mDialog).setText(ROAMING_INFO_VALUE_ID, roamingOnString);
    }

    @Test
    public void initialize_updateRoamingStatusNotRoaming_shouldSetSettingToRoamingOff() {
        when(mServiceState.getRoaming()).thenReturn(false);

        mController.initialize();

        final String roamingOffString = mContext.getString(R.string.radioInfo_roaming_not);
        verify(mDialog).setText(ROAMING_INFO_VALUE_ID, roamingOffString);
    }

    @Test
    public void initialize_doNotShowIccid_shouldRemoveIccidSetting() {
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_ICCID_IN_SIM_STATUS_BOOL)).thenReturn(false);

        mController.initialize();

        verify(mDialog).removeSettingFromScreen(ICCID_INFO_LABEL_ID);
        verify(mDialog).removeSettingFromScreen(ICCID_INFO_VALUE_ID);
    }

    @Test
    public void initialize_doNotShowSignalStrength_shouldRemoveSignalStrengthSetting() {
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL))
            .thenReturn(false);

        mController.initialize();

        verify(mDialog).removeSettingFromScreen(SIGNAL_STRENGTH_LABEL_ID);
        verify(mDialog).removeSettingFromScreen(SIGNAL_STRENGTH_VALUE_ID);
    }

    @Test
    public void initialize_showSignalStrengthAndIccId_shouldShowSignalStrengthAndIccIdSetting() {
        // getConfigForSubId is nullable, so make sure the default behavior is correct
        when(mCarrierConfigManager.getConfigForSubId(anyInt())).thenReturn(null);

        mController.initialize();

        verify(mDialog).setText(eq(SIGNAL_STRENGTH_VALUE_ID), any());
        verify(mDialog).removeSettingFromScreen(ICCID_INFO_LABEL_ID);
        verify(mDialog).removeSettingFromScreen(ICCID_INFO_VALUE_ID);
    }

    @Test
    public void initialize_showIccid_shouldSetIccidToSetting() {
        final String iccid = "12351351231241";
        when(mPersistableBundle.getBoolean(
                CarrierConfigManager.KEY_SHOW_ICCID_IN_SIM_STATUS_BOOL)).thenReturn(true);
        doReturn(iccid).when(mController).getSimSerialNumber(anyInt());

        mController.initialize();

        verify(mDialog).setText(ICCID_INFO_VALUE_ID, iccid);
    }

    @Test
    public void initialize_updateEid_shouldNotSetEid() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(PhoneConstants.MAX_PHONE_COUNT_DUAL_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo1 = new UiccCardInfo(
                false,                                  // isEuicc
                0,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                0,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo1);
        UiccCardInfo uiccCardInfo2 = new UiccCardInfo(
                true,                                   // isEuicc
                1,                                      // cardId
                null,                                   // eid (unavailable)
                null,                                   // iccid
                1,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo2);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 1);
        slotMapping.put(1, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(null);

        mController.initialize();

        // Keep 'Not available' if neither the card nor the associated manager can provide EID.
        verify(mDialog, never()).setText(eq(EID_INFO_VALUE_ID), any());
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldSetEidFromCard() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(PhoneConstants.MAX_PHONE_COUNT_DUAL_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo1 = new UiccCardInfo(
                true,                                   // isEuicc
                0,                                      // cardId
                TEST_EID_FROM_CARD,                     // eid
                null,                                   // iccid
                0,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo1);
        UiccCardInfo uiccCardInfo2 = new UiccCardInfo(
                false,                                  // isEuicc
                1,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                1,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo2);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        slotMapping.put(1, 1);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(TEST_EID_FROM_MANAGER);

        mController.initialize();

        // Set EID retrieved from the card.
        verify(mDialog).setText(EID_INFO_VALUE_ID, TEST_EID_FROM_CARD);
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldSetEidFromManager() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(PhoneConstants.MAX_PHONE_COUNT_DUAL_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo1 = new UiccCardInfo(
                false,                                  // isEuicc
                0,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                0,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo1);
        UiccCardInfo uiccCardInfo2 = new UiccCardInfo(
                true,                                   // isEuicc
                1,                                      // cardId
                null,                                   // eid (unavailable)
                null,                                   // iccid
                1,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo2);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 1);
        slotMapping.put(1, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(TEST_EID_FROM_MANAGER);
        when(mEuiccManager.createForCardId(0)).thenThrow(
                new RuntimeException("Unexpected card ID was specified"));
        when(mEuiccManager.createForCardId(1)).thenReturn(mEuiccManager);

        mController.initialize();

        // Set EID retrieved from the manager associated with the card which cannot provide EID.
        verify(mDialog).setText(EID_INFO_VALUE_ID, TEST_EID_FROM_MANAGER);
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldRemoveEid() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(PhoneConstants.MAX_PHONE_COUNT_DUAL_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo1 = new UiccCardInfo(
                false,                                  // isEuicc
                0,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                0,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo1);
        UiccCardInfo uiccCardInfo2 = new UiccCardInfo(
                true,                                   // isEuicc
                1,                                      // cardId
                TEST_EID_FROM_CARD,                     // eid
                null,                                   // iccid
                1,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo2);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        slotMapping.put(1, 1);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(TEST_EID_FROM_MANAGER);

        mController.initialize();

        // Remove EID if the card is not eUICC.
        verify(mDialog, never()).setText(eq(EID_INFO_VALUE_ID), any());
        verify(mDialog).removeSettingFromScreen(eq(EID_INFO_LABEL_ID));
        verify(mDialog).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldNotSetEidInSingleSimMode() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(
                PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo = new UiccCardInfo(
                true,                                   // isEuicc
                0,                                      // cardId
                TEST_EID_FROM_CARD,                     // eid (not used)
                null,                                   // iccid
                0,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(null);

        mController.initialize();

        // Keep 'Not available' if the default eUICC manager cannot provide EID in Single SIM mode.
        verify(mDialog, never()).setText(eq(EID_INFO_VALUE_ID), any());
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldSetEidInSingleSimModeWithEnabledEuicc() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(
                PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo = new UiccCardInfo(
                true,                                   // isEuicc (eUICC slot is selected)
                0,                                      // cardId
                TEST_EID_FROM_CARD,                     // eid (not used)
                null,                                   // iccid
                0,                                      // slotIndex
                false);                                 // isRemovable
        uiccCardInfos.add(uiccCardInfo);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(TEST_EID_FROM_MANAGER);
        when(mEuiccManager.createForCardId(anyInt())).thenThrow(
                new RuntimeException("EID shall be retrieved from the default eUICC manager"));

        mController.initialize();

        // Set EID retrieved from the default eUICC manager in Single SIM mode.
        verify(mDialog).setText(EID_INFO_VALUE_ID, TEST_EID_FROM_MANAGER);
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldSetEidInSingleSimModeWithDisabledEuicc() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(
                PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo = new UiccCardInfo(
                false,                                  // isEuicc (eUICC slot is not selected)
                0,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                0,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(true);
        when(mEuiccManager.getEid()).thenReturn(TEST_EID_FROM_MANAGER);
        when(mEuiccManager.createForCardId(anyInt())).thenThrow(
                new RuntimeException("EID shall be retrieved from the default eUICC manager"));

        mController.initialize();

        // Set EID retrieved from the default eUICC manager in Single SIM mode.
        verify(mDialog).setText(EID_INFO_VALUE_ID, TEST_EID_FROM_MANAGER);
        verify(mDialog, never()).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_updateEid_shouldRemoveEidInSingleSimMode() {
        when(mTelephonyManager.getPhoneCount()).thenReturn(
                PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM);

        ArrayList<UiccCardInfo> uiccCardInfos = new ArrayList<>();
        UiccCardInfo uiccCardInfo = new UiccCardInfo(
                false,                                  // isEuicc
                0,                                      // cardId
                null,                                   // eid
                "123451234567890",                      // iccid
                0,                                      // slotIndex
                true);                                  // isRemovable
        uiccCardInfos.add(uiccCardInfo);
        when(mTelephonyManager.getUiccCardsInfo()).thenReturn(uiccCardInfos);

        Map<Integer, Integer> slotMapping = new HashMap<>();
        slotMapping.put(0, 0);
        when(mTelephonyManager.getLogicalToPhysicalSlotMapping()).thenReturn(slotMapping);

        when(mEuiccManager.isEnabled()).thenReturn(false);
        when(mEuiccManager.getEid()).thenReturn(null);

        mController.initialize();

        // Remove EID if the default eUICC manager indicates that eSIM is not enabled.
        verify(mDialog, never()).setText(eq(EID_INFO_VALUE_ID), any());
        verify(mDialog).removeSettingFromScreen(eq(EID_INFO_LABEL_ID));
        verify(mDialog).removeSettingFromScreen(eq(EID_INFO_VALUE_ID));
    }

    @Test
    public void initialize_imsRegistered_shouldSetImsRegistrationStateSummaryToRegisterd() {
        when(mPersistableBundle.getBoolean(
            CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL)).thenReturn(true);
        when(mTelephonyManager.isImsRegistered(anyInt())).thenReturn(true);

        mController.initialize();

        verify(mDialog).setText(IMS_REGISTRATION_STATE_VALUE_ID,
            mContext.getString(R.string.ims_reg_status_registered));
    }

    @Test
    public void initialize_imsNotRegistered_shouldSetImsRegistrationStateSummaryToNotRegisterd() {
        when(mPersistableBundle.getBoolean(
            CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL)).thenReturn(true);
        when(mTelephonyManager.isImsRegistered(anyInt())).thenReturn(false);

        mController.initialize();

        verify(mDialog).setText(IMS_REGISTRATION_STATE_VALUE_ID,
            mContext.getString(R.string.ims_reg_status_not_registered));
    }

    @Test
    public void initialize_showImsRegistration_shouldNotRemoveImsRegistrationStateSetting() {
        when(mPersistableBundle.getBoolean(
            CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL)).thenReturn(true);

        mController.initialize();

        verify(mDialog, never()).removeSettingFromScreen(IMS_REGISTRATION_STATE_VALUE_ID);
    }

    @Test
    public void initialize_doNotShowImsRegistration_shouldRemoveImsRegistrationStateSetting() {
        when(mPersistableBundle.getBoolean(
            CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL)).thenReturn(false);

        mController.initialize();

        verify(mDialog).removeSettingFromScreen(IMS_REGISTRATION_STATE_LABEL_ID);
        verify(mDialog).removeSettingFromScreen(IMS_REGISTRATION_STATE_VALUE_ID);
    }

    @Test
    public void initialize_nullSignalStrength_noCrash() {
        doReturn(null).when(mController).getSignalStrength();
        // we should not crash when running the following line
        mController.initialize();
    }
}
