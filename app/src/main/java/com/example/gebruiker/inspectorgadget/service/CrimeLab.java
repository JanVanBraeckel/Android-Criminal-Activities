package com.example.gebruiker.inspectorgadget.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.gebruiker.inspectorgadget.database.Crime;
import com.example.gebruiker.inspectorgadget.database.CrimeDao;
import com.example.gebruiker.inspectorgadget.database.DaoMaster;
import com.example.gebruiker.inspectorgadget.database.DaoSession;

import java.io.File;
import java.util.List;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private CrimeDao mCrimeDao;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        initDatabase();
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "crimelab-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        mCrimeDao = daoSession.getCrimeDao();
    }


    public void addCrime(Crime c) {
        mCrimeDao.insert(c);
    }

    public List<Crime> getCrimes() {
        return mCrimeDao.queryBuilder()
                .list();
    }

    public Crime getCrime(long id) {
        return mCrimeDao.queryBuilder()
                .where(CrimeDao.Properties.Id.eq(id))
                .unique();
    }

    public void updateCrime(Crime crime) {
        mCrimeDao.update(crime);
    }

    public void removeCrime(Crime crime) {
        mCrimeDao.delete(crime);
    }
}
