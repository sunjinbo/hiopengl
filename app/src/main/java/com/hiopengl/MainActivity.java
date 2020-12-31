package com.hiopengl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private ExampleCategoryAdapter mCategoryAdapter;
    private List<ExampleCategory> mAllExamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_view);
        mCategoryAdapter = new ExampleCategoryAdapter(this, R.layout.list_view_item);
        mAllExamples = getAllExamples();
        mCategoryAdapter.addAll(mAllExamples);
        mListView.setAdapter(mCategoryAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mListView.getAdapter() instanceof ExampleInfoAdapter) {
            mListView.setAdapter(mCategoryAdapter);
        } else {
            super.onBackPressed();
        }
    }

    private List<ExampleCategory> getAllExamples() {
        List<ExampleCategory> allExamples = new ArrayList<>();

        ExampleCategory android = new ExampleCategory("Android + OpenGL ES");
        android.add("GLSurfaceView + OpenGL ES", "com.hiopengl.OpenGLGLSurfaceViewActivity", true);
        android.add("TextureView + OpenGL ES", "com.hiopengl.OpenGLTextureViewActivity", true);
        android.add("SurfaceView + OpenGL ES", "com.hiopengl.OpenGLSurfaceViewActivity", true);
        android.add("SurfaceTexture + OpenGL ES", "com.hiopengl.OpenGLSurfaceTextureActivity", false);
        android.add("View + Canvas", "com.hiopengl.CanvasViewActivity", true);
        android.add("SurfaceView + Canvas", "com.hiopengl.CanvasSurfaceViewActivity", true);
        android.add("TextureView + Canvas", "com.hiopengl.CanvasTextureViewActivity", true);
        allExamples.add(android);

        ExampleCategory gettingStarted = new ExampleCategory("Getting Started");
        gettingStarted.add("Geometric Figures", "com.hiopengl.GeometricActivity", true);
        gettingStarted.add("Polyhedron", "com.hiopengl.PolyhedronActivity", true);
        gettingStarted.add("OpenGL GLSL", "com.hiopengl.GLSLActivity", true);
        gettingStarted.add("Modeling and Viewing", "com.hiopengl.ViewModelActivity", true);
        gettingStarted.add("Projection", "com.hiopengl.ProjectionActivity", true);
        gettingStarted.add("Viewport", "com.hiopengl.ViewportActivity", true);
        gettingStarted.add("VA、VBO、VAO、EBO", "com.hiopengl.VertexActivity", true);
        gettingStarted.add("Texture 2D", "com.hiopengl.Texture2DActivity", true);
        gettingStarted.add("Texture 3D", "com.hiopengl.Texture3DActivity", true);
        gettingStarted.add("Scissor test", "com.hiopengl.ScissorTestActivity", true);
        allExamples.add(gettingStarted);

        ExampleCategory advancedOpenGL = new ExampleCategory("Advanced OpenGL");
        advancedOpenGL.add("Multiple shader", "com.hiopengl.MultipleShaderActivity", true);
        advancedOpenGL.add("Face culling", "com.hiopengl.FaceCullingActivity", true);
        advancedOpenGL.add("Blending", "com.hiopengl.BlendingActivity", true);
        advancedOpenGL.add("Mesh", "com.hiopengl.MeshActivity", false);
        advancedOpenGL.add("Frame Buffer", "com.hiopengl.FrameBufferActivity", false);
        advancedOpenGL.add("Depth Test", "com.hiopengl.DepthTestActivity", true);
        advancedOpenGL.add("Stencil Test", "com.hiopengl.StencilTestActivity", true);
        allExamples.add(advancedOpenGL);

        ExampleCategory lighting = new ExampleCategory("Lighting");
        lighting.add("Phong Lighting", "com.hiopengl.PhongLightingActivity", true);
        lighting.add("Lighting source", "com.hiopengl.LightingSourceActivity", true);
        lighting.add("Lighting maps", "", false);
        lighting.add("Material", "com.hiopengl.MaterialActivity", true);
        allExamples.add(lighting);

        ExampleCategory inPractice = new ExampleCategory("In Practice");
        inPractice.add("Particle", "com.hiopengl.ParticleActivity", true);
        allExamples.add(inPractice);

        return allExamples;
    }

    private class ExampleCategoryAdapter extends ArrayAdapter<ExampleCategory> {

        private LayoutInflater mInflater;

        public ExampleCategoryAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_view_item, null);
                holder = new ViewHolder();

                holder.item = convertView.findViewById(R.id.item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final ExampleCategory category = getItem(position);
            holder.item.setText(category.getCategoryName());

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExampleInfoAdapter adapter = new ExampleInfoAdapter(getContext(), R.layout.list_view_item);
                    adapter.addAll(category.getExamples());
                    mListView.setAdapter(adapter);
                }
            });

            return convertView;
        }

        public class ViewHolder {
            public Button item;
        }
    }

    private class ExampleInfoAdapter extends ArrayAdapter<ExampleInfo> {

        private LayoutInflater mInflater;

        public ExampleInfoAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_view_item, null);
                holder = new ViewHolder();

                holder.item = convertView.findViewById(R.id.item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final ExampleInfo exampleInfo = getItem(position);
            holder.item.setText(exampleInfo.getExampleName());
            holder.item.setEnabled(exampleInfo.isDone());
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    ComponentName cn = new ComponentName(getContext(), exampleInfo.getActivityFrom());
                    intent.setComponent(cn);
                    intent.putExtra("title", exampleInfo.getExampleName());
                    startActivity(intent);
                }
            });

            return convertView;
        }

        public class ViewHolder {
            public Button item;
        }
    }

    private class ExampleCategory {
        private String mCategoryName;
        private List<ExampleInfo> mExamples = new ArrayList<>();

        public ExampleCategory(String categoryName) {
            mCategoryName = categoryName;
        }

        public String getCategoryName() {
            return mCategoryName;
        }

        public List<ExampleInfo> getExamples() {
            return mExamples;
        }

        public void add(String exampleName, String activityFrom, boolean isDone) {
            mExamples.add(new ExampleInfo(exampleName, activityFrom, isDone));
        }
    }

    private static class ExampleInfo {
        private String mExampleName;
        private String mActivityFrom;
        private boolean mIsDone;

        public ExampleInfo(String exampleName, String activityFrom, boolean isDone) {
            mExampleName = exampleName;
            mActivityFrom = activityFrom;
            mIsDone = isDone;
        }

        public String getExampleName() {
            return mExampleName;
        }

        public String getActivityFrom() {
            return mActivityFrom;
        }

        public boolean isDone() {
            return mIsDone;
        }
    }
}