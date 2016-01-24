/**
 *   ownCloud Android client application
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2016  Tobias Kaminsky
 *   Copyright (C) 2016 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.ui.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.storage.StorageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.datamodel.ThumbnailsCacheManager;
import com.owncloud.android.datamodel.ThumbnailsCacheManager.AsyncDrawable;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.utils.DisplayUtils;
import com.owncloud.android.utils.MimetypeIconUtil;

public class ImageSimpleAdapter extends SimpleAdapter {
    
    private Context mContext;
    private Account mAccount;
    private FileDataStorageManager mStorageManager;
    private int mResource;
    private LayoutInflater inflater;

    public ImageSimpleAdapter(Context context,
            List<? extends Map<String, ?>> data, int resource, String[] from,
            int[] to, FileDataStorageManager storageManager, Account account) {
        super(context, data, resource, from, to);
        mAccount = account;
        mStorageManager = storageManager;
        mContext = context;
        mResource = resource;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(mResource, null);

        HashMap<String, OCFile> data = (HashMap<String, OCFile>) getItem(position);
        OCFile file = data.get("dirname");

        TextView filename = (TextView) vi.findViewById(R.id.filename);
        filename.setText((CharSequence) file.getFileName());
        
        ImageView fileIcon = (ImageView) vi.findViewById(R.id.thumbnail);
        fileIcon.setTag(file.getFileId());

        // TODO enable after #1277 is merged
//        TextView lastModV = (TextView) vi.findViewById(R.id.last_mod);
//        lastModV.setText(DisplayUtils.getRelativeTimestamp(mContext, file));
        
        // get Thumbnail if file is image
        if (file.isImage() && file.getRemoteId() != null){
             // Thumbnail in Cache?
            Bitmap thumbnail = ThumbnailsCacheManager.getBitmapFromDiskCache(
                    String.valueOf(file.getRemoteId())
            );
            if (thumbnail != null && !file.needsUpdateThumbnail()){
                fileIcon.setImageBitmap(thumbnail);
            } else {
                // generate new Thumbnail
                if (ThumbnailsCacheManager.cancelPotentialWork(file, fileIcon)) {
                    final ThumbnailsCacheManager.ThumbnailGenerationTask task = 
                            new ThumbnailsCacheManager.ThumbnailGenerationTask(fileIcon, mStorageManager, 
                                    mAccount);
                    if (thumbnail == null) {
                        thumbnail = ThumbnailsCacheManager.mDefaultImg;
                    }
                    final AsyncDrawable asyncDrawable = new AsyncDrawable(
                            mContext.getResources(), 
                            thumbnail, 
                            task
                    );
                    fileIcon.setImageDrawable(asyncDrawable);
                    task.execute(file);
                }
            }
        } else {
            fileIcon.setImageResource(
                    MimetypeIconUtil.getFileTypeIconId(file.getMimetype(), file.getFileName())
            );
        }
        return vi;
    }


}
