package com.bignerdranch.android.criminalintent.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class Utils {
  public void handleContactSelection(Intent data, Context context) {
    if (data != null) {
      Uri uri = data.getData();
      if (uri != null) {
        Cursor cursor = null;
        Cursor nameCursor = null;
        try {
          cursor = context.getContentResolver().query(uri, new String[]{
                  ContactsContract.CommonDataKinds.Phone.NUMBER
//                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID
              },
              null, null, null);

          String phoneNumber = null;
//          String contactId = null;
          if (cursor != null && cursor.moveToFirst()) {
//            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          }

//          String givenName = null;///first name.
//          String familyName = null;//last name.
//
//          String projection[] = new String[]{ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
//              ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME};
//          String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " +
//              ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
//          String[] whereNameParams = new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId};
//
//          nameCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
//              projection, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
//
//          if (nameCursor != null && nameCursor.moveToNext()) {
//            givenName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
//            familyName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
//          }

//          doSomething(phoneNumber, givenName, familyName);
          doSomething(phoneNumber, "givenName", "familyName");

        } finally {
          if (cursor != null) {
            cursor.close();
          }

          if (nameCursor != null) {
            nameCursor.close();
          }
        }
      }
    }
  }

  private void doSomething(String phoneNumber, String givenName, String familyName) {
    Log.d("AAA", phoneNumber + " " + givenName + " " + familyName);
  }
}
