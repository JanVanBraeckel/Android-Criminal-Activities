package com.example.gebruiker.inspectorgadget;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.example.gebruiker.inspectorgadget.database.Crime;
import com.example.gebruiker.inspectorgadget.service.CrimeLab;
import com.example.gebruiker.inspectorgadget.service.ICrimeLab;

import org.bouncycastle.asn1.BERNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.util.ActivityController;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.Assert.*;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class CrimeListActivityTest {
    private CrimeListActivity crimeListActivity;
    private ActivityController<CrimeListActivity> controller;
    private ICrimeLab crimeLab;

    @Before
    public void setup() {
        controller = Robolectric.buildActivity(CrimeListActivity.class);
        crimeLab = new MockCrimeLab();
        CrimeLab.setTestingInstance(crimeLab);

        crimeListActivity = controller
                .create()
                .start()
                .resume()
                .visible()
                .get();
    }

    @After
    public void finishComponentTesting() {
        // sInstance is the static variable name which holds the singleton instance
        resetSingleton(CrimeLab.class, "sCrimeLab");
    }

    private void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void clickSubtitleButton(){
        assertNull(crimeListActivity.getSupportActionBar().getSubtitle());

        ShadowActivity shadowActivity = Shadows.shadowOf(crimeListActivity);
        shadowActivity.clickMenuItem(R.id.menu_item_show_subtitle);
        assertEquals("10 crimes", crimeListActivity.getSupportActionBar().getSubtitle().toString());

        shadowActivity.clickMenuItem(R.id.menu_item_show_subtitle);
        assertNull(crimeListActivity.getSupportActionBar().getSubtitle());
    }

    @Test
    public void selectCrimeTransfersPayload() {
        ShadowActivity shadowActivity = Shadows.shadowOf(crimeListActivity);

        Crime mock = crimeLab.getCrimes().get(0);

        CrimeListFragment listFragment = (CrimeListFragment) crimeListActivity.getSupportFragmentManager().findFragmentByTag(SingleFragmentActivity.FRAGMENT_TAG);
        listFragment.mCallbacks.onCrimeSelected(mock);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals((long) mock.getId(), startedIntent.getLongExtra("com.example.gebruiker.inspectorgadget.crime_id", Integer.MIN_VALUE));
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);

        assertEquals(shadowIntent.getComponent().getClassName(), CrimePagerActivity.class.getName());
    }

    @Test
    public void addEntryStartsIntent() {
        ShadowActivity shadowActivity = Shadows.shadowOf(crimeListActivity);
        shadowActivity.clickMenuItem(R.id.menu_item_new_crime);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertEquals(shadowIntent.getComponent().getClassName(), CrimePagerActivity.class.getName());
    }

    @Test
    public void recreateSavesState() {
        Crime mockCrime = crimeLab.getCrime(1L);

        CrimeFragment crimeFragment = CrimeFragment.newInstance(1L);
        FragmentManager fragmentManager = crimeListActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(crimeFragment, CrimeFragment.TAG).commit();

        assertEquals(mockCrime.getTitle(), crimeFragment.mTitleField.getText().toString());
        assertEquals(mockCrime.getSolved(), crimeFragment.mSolvedCheckbox.isChecked());
        assertEquals(mockCrime.getDate().toString(), crimeFragment.mDateButton.getText().toString());
        assertEquals(mockCrime.getSuspect(), crimeFragment.mSuspectButton.getText().toString());

        Bundle bundle = new Bundle();
        controller
                .saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        controller = Robolectric.buildActivity(CrimeListActivity.class)
                .create(bundle)
                .start()
                .restoreInstanceState(bundle)
                .resume()
                .visible();

        crimeListActivity = controller.get();

        fragmentManager = crimeListActivity.getSupportFragmentManager();
        crimeFragment = (CrimeFragment) fragmentManager.findFragmentByTag(CrimeFragment.TAG);

        assertEquals(mockCrime.getTitle(), crimeFragment.mTitleField.getText().toString());
        assertEquals(mockCrime.getSolved(), crimeFragment.mSolvedCheckbox.isChecked());
        assertEquals(mockCrime.getDate().toString(), crimeFragment.mDateButton.getText().toString());
        assertEquals(mockCrime.getSuspect(), crimeFragment.mSuspectButton.getText().toString());
    }

    @After
    public void tearDown() {
        controller
                .pause()
                .stop()
                .destroy();
    }
}