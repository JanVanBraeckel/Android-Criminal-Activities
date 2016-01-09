package com.example.gebruiker.inspectorgadget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gebruiker.inspectorgadget.components.DatePickerFragment;
import com.example.gebruiker.inspectorgadget.components.TouchHighlightImageButton;
import com.example.gebruiker.inspectorgadget.database.Crime;
import com.example.gebruiker.inspectorgadget.service.CrimeLab;

import java.io.File;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CrimeFragment extends Fragment {

    public static final String TAG = "CrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    @Bind(R.id.crime_title)
    EditText mTitleField;
    @Bind(R.id.crime_date)
    Button mDateButton;
    @Bind(R.id.crime_solved)
    CheckBox mSolvedCheckbox;
    @Bind(R.id.crime_photo)
    TouchHighlightImageButton mPhotoView;
    @Bind(R.id.crime_suspect)
    Button mSuspectButton;
    private Callbacks mCallbacks;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    @Bind(R.id.expanded_image)
    ImageView expandedImageView;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(long crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long crimeId = (long) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        ButterKnife.bind(this, v);

        mTitleField.setText(mCrime.getTitle());

        updateDate();

        mSolvedCheckbox.setChecked(mCrime.getSolved());

        Glide.with(getContext())
                .load(mCrime.getPicturePath())
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(mPhotoView);

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mSuspectButton.setText(mCrime.getSuspect());

        return v;
    }

    @OnClick(R.id.crime_photo)
    public void crimePhotoClicked() {
        if (mCrime.getPicturePath() != null && !mCrime.getPicturePath().isEmpty()) {
            zoomImageFromThumb(mPhotoView);
        }
    }

    @OnClick(R.id.crime_suspect)
    public void crimeSuspectClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_CONTACT);
    }

    @OnClick(R.id.crime_camera)
    public void cameraButtonClicked() {
        String imagePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath() + "/" + mCrime.getPhotoFilename();
        File imageFile = new File(imagePath);
        Uri imageOutputUri = Uri.fromFile(imageFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageOutputUri);

        if (mCrime.getPicturePath() != null && !mCrime.getPicturePath().isEmpty()) {
            File deleteFile = new File(mCrime.getPicturePath());
            deleteFile.delete();
        }

        mCrime.setPicturePath(imagePath);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_PHOTO);

    }

    @OnClick(R.id.crime_report)
    public void crimeReportClicked() {
        if (mCrime.getPicturePath() == null || mCrime.getPicturePath().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.attachPicture), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File file = new File(mCrime.getPicturePath());
        Uri uri = Uri.fromFile(file);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mCrime.getTitle());
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(shareIntent);
    }

    @OnTextChanged(value = R.id.crime_title, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void crimeTitleTextChanged(CharSequence text) {
        if (getActivity() == null) {
            return;
        }
        mCrime.setTitle(text.toString());
        updateCrime();
    }

    @OnCheckedChanged(R.id.crime_solved)
    public void crimeSolvedChecked(boolean checked) {
        mCrime.setSolved(checked);
        updateCrime();
    }

    @OnClick(R.id.crime_date)
    public void crimeDateClicked() {
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialog = DatePickerFragment
                .newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
        dialog.show(manager, DIALOG_DATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            // Perform your query - the contactUri is like a "where"
            // clause here
            ContentResolver resolver = getActivity().getContentResolver();
            Cursor c = resolver.query(contactUri, queryFields, null, null, null);

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();

                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.getSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        return getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
    }

    private void updatePhotoView() {
        Glide.with(getContext())
                .load(mCrime.getPicturePath())
                .centerCrop()
                .into(mPhotoView);
    }

    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        Glide.with(getContext())
                .load(mCrime.getPicturePath())
                .into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        ButterKnife.findById(getView(), R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(view -> {
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel, back to their
            // original values.
            AnimatorSet set1 = new AnimatorSet();
            set1
                    .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
            set1.setDuration(mShortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }
            });
            set1.start();
            mCurrentAnimator = set1;
        });
    }
}
