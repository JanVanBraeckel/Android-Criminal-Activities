package com.example.gebruiker.inspectorgadget.service;

import android.content.Context;
import android.database.ContentObserver;

import com.example.gebruiker.inspectorgadget.database.Crime;

import java.util.List;

/**
 * Created by Jan on 9/01/2016.
 */
public interface ICrimeLab {
    void addCrime(Crime c);
    List<Crime> getCrimes();
    Crime getCrime(long id);
    void updateCrime(Crime crime);
    void removeCrime(Crime crime);
}
