package com.android.codeflaunt.piyush.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Piyush on 21-Feb-16.
 */
public class CrimeFragment extends Fragment {

    private static final String DIALOG_IAMGE = "DialogImage";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String ARG_CRIME_ID = "crime_id";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private MenuItem mCallSuspectItem;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }


    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);

        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
         View v = inflater.inflate(R.layout.fragment_crime, container, false);

         mTitleField = (EditText) v.findViewById(R.id.crime_title);
         mTitleField.setText(mCrime.getTitle());
         mTitleField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {

             }

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {

                 mCrime.setTitle(s.toString());
                 updateCrime();
             }

             @Override
             public void afterTextChanged(Editable s) {

             }
         });

         mDateButton = (Button) v.findViewById(R.id.crime_date);
         updateDate();
         mDateButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 FragmentManager manager = getFragmentManager();
                 DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                 dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                 dialog.show(manager, DIALOG_DATE);
             }
         });

         mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
         mSolvedCheckBox.setChecked(mCrime.isSolved());
         mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 mCrime.setSolved(isChecked);
                 updateCrime();
             }
         });

         mReportButton = (Button) v.findViewById(R.id.crime_report);
         mReportButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 Intent i = ShareCompat.IntentBuilder.from(getActivity())
                         .setType("text/plain")
                         .setText(getCrimeReport())
                         .setSubject(getString(R.string.crime_report_subject))
                         .setChooserTitle(getString(R.string.send_report))
                         .getIntent();
                 startActivity(i);
             }
         });

         final Intent pickContact = new Intent(Intent.ACTION_PICK,
                 ContactsContract.Contacts.CONTENT_URI);
         mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
         mSuspectButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivityForResult(pickContact, REQUEST_CONTACT);
             }
         });

         if(mCrime.getSuspect() != null){
             mSuspectButton.setText(mCrime.getSuspect());
         }

         PackageManager packageManager = getActivity().getPackageManager();
         if(packageManager.resolveActivity(pickContact,
                 PackageManager.MATCH_DEFAULT_ONLY) == null){
             mSuspectButton.setEnabled(false);
         }

         mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
         final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

         boolean canTakePhoto = mPhotoFile != null &&
                 captureImage.resolveActivity(packageManager) != null;
         mPhotoButton.setEnabled(canTakePhoto);

         if(canTakePhoto){
             Uri uri = Uri.fromFile(mPhotoFile);
             captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
         }

         mPhotoButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivityForResult(captureImage, REQUEST_PHOTO);
             }
         });

         mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
         updatePhotoView();
         ViewTreeObserver observer = mPhotoView.getViewTreeObserver();

         observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 updatePhotoView();
             }
         });
         mPhotoView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 FragmentManager manager = getActivity()
                         .getSupportFragmentManager();
                 ImageViewerFragment dialog = ImageViewerFragment.newInstance(mPhotoFile);
                 dialog.show(manager, DIALOG_IAMGE);
             }
         });

         return v;
     }

    public void onPause(){
        super.onPause();


        CrimeLab.get(getActivity())
                .updateCrime(mCrime);

    }

    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }else if(requestCode == REQUEST_CONTACT &&  data != null){
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try{
                if(c.getCount() == 0)
                    return;

                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                retrieveContactNumber(contactUri);
                updateCrime();
                mSuspectButton.setText(suspect);
            }finally {
                c.close();
            }

        }else if(requestCode == REQUEST_PHOTO){
            updateCrime();
            updatePhotoView();
        }
    }

    private void retrieveContactNumber(Uri uriContact) {

        String contactNumber = null;
        String contactID = null;
        // getting contacts ID
        Cursor cursorID = getActivity().getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getActivity()
                .getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                        new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        mCrime.setSuspectNo(contactNumber);

        cursorPhone.close();
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setEnabled(false);
        }else{
            Bitmap bitmap;
            if (mPhotoView == null) {
                bitmap = PictureUtils.getScaledBitmap(
                        mPhotoFile.getPath(), getActivity());
            }else{
                bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView);
            }
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(true);
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getStringDate());
    }

    private String getCrimeReport(){
        String solvedString = null;

        if(mCrime.isSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null)
            suspect = getString(R.string.crime_report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect, suspect);

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);

        mCallSuspectItem = menu.findItem(R.id.menu_item_call_suspect);

        updateMenu();
    }

    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.menu_item_delete_crime :
                CrimeLab.get(getActivity()).removeCrime(mCrime);
                getActivity().finish();
                return true;
            case R.id.menu_item_call_suspect :
                getActivity().invalidateOptionsMenu();
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + mCrime.getSuspectNo()));
                startActivity(i);
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void updateMenu(){
        if(mCallSuspectItem != null){
            if(mCrime.getSuspectNo() == null)
                mCallSuspectItem.setVisible(false);
            else
                mCallSuspectItem.setVisible(true);
        }
    }

    public void onResume(){
        super.onResume();
        updateMenu();
    }

}
