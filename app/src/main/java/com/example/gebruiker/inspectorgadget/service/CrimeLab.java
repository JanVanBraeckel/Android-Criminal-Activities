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

public class CrimeLab implements ICrimeLab {
    private static ICrimeLab sCrimeLab;

    private Context mContext;
    private CrimeDao mCrimeDao;

    public static ICrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public static void setTestingInstance(ICrimeLab crimeLab){
        sCrimeLab = crimeLab;
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

    @Override
    public void addCrime(Crime c) {
        mCrimeDao.insert(c);
    }

    @Override
    public List<Crime> getCrimes() {
        return mCrimeDao.queryBuilder()
                .list();
    }

    @Override
    public Crime getCrime(long id) {
        return mCrimeDao.queryBuilder()
                .where(CrimeDao.Properties.Id.eq(id))
                .unique();
    }

    @Override
    public void updateCrime(Crime crime) {
        mCrimeDao.update(crime);
    }

    @Override
    public void removeCrime(Crime crime) {
        mCrimeDao.delete(crime);
    }
}
