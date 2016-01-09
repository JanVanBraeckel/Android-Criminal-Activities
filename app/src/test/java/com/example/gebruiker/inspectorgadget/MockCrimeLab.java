package com.example.gebruiker.inspectorgadget;

import com.example.gebruiker.inspectorgadget.database.Crime;
import com.example.gebruiker.inspectorgadget.service.ICrimeLab;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Jan on 9/01/2016.
 */
public class MockCrimeLab implements ICrimeLab {
    private List<Crime> crimes = new ArrayList<>();

    public MockCrimeLab(){
        for(int i= 1; i <= 10; i++){
            crimes.add(new Crime((long) i, "Test Crime " + i, new Date(), false, "Jan Van Braeckel", ""));
        }
    }

    @Override
    public void addCrime(Crime c) {
        crimes.add(c);
    }

    @Override
    public List<Crime> getCrimes() {
        return crimes;
    }

    @Override
    public Crime getCrime(long id) {
        for(Crime crime: crimes){
            if(crime.getId() == id) {
                return crime;
            }
        }

        return null;
    }

    @Override
    public void updateCrime(Crime crime) {
        for(Iterator<Crime> it = crimes.iterator(); it.hasNext();){
            Crime c = it.next();
            if(c.getId() == crime.getId()){
                it.remove();
            }
        }
        crimes.add(crime);
    }

    @Override
    public void removeCrime(Crime crime) {
        crimes.remove(crime);
    }
}
