package com.hiopengl.advanced.model;

import android.content.Context;

public class Plane extends Object3D {
    protected float mWidth;
    protected float mHeight;
    protected int mSegmentsW;
    protected int mSegmentsH;

    public Plane(Context context, float width, float height, int segmentsW, int segmentsH) {
        super(context);
        mWidth = width;
        mHeight = height;
        mSegmentsW = segmentsW;
        mSegmentsH = segmentsH;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Plane;
    }

    private void initVertex() {
        int i, j;
        mNumVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
        float[] vertices = new float[mNumVertices * 3];
        int vertexCount = 0;

        short[] indices = new short[mSegmentsW * mSegmentsH * 6];

        for (i = 0; i <= mSegmentsW; i++) {
            for (j = 0; j <= mSegmentsH; j++) {
                float v1 = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
                float v2 = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
                vertices[vertexCount] = v1;
                vertices[vertexCount + 1] = v2;
                vertices[vertexCount + 2] = 0;

                vertexCount += 3;
            }
        }

        int colspan = mSegmentsH + 1;
        int indexCount = 0;
        for (int col = 0; col < mSegmentsW; col++) {
            for (int row = 0; row < mSegmentsH; row++) {
                int ul = col * colspan + row;
                int ll = ul + 1;
                int ur = (col + 1) * colspan + row;
                int lr = ur + 1;

                indices[indexCount++] = (short)ur;
                indices[indexCount++] = (short)lr;
                indices[indexCount++] = (short)ul;

                indices[indexCount++] = (short)lr;
                indices[indexCount++] = (short)ll;
                indices[indexCount++] = (short)ul;
            }
        }

        float[] VERTICES = new float[] {-0.6081204068324845f,0.6081204068324845f,0.510273594836914f,-0.5531064412306825f,0.6381997497028097f,0.5355131596407324f,-0.48977498010487086f,0.6678749971635558f,0.5604136481448803f,-0.41762559367376817f,0.6960427025390038f,0.5840491586765901f,-0.33662412251131973f,0.7213374105352763f,0.6052739382343718f,-0.2474096139564977f,0.7422288789915019f,0.622803961221278f,-0.15144180218491424f,0.7572090298601859f,0.6353737999392022f,-0.05100316290307633f,0.7650474387632372f,0.6419510057751238f,0.05100316290307633f,0.7650474387632372f,0.6419510057751238f,0.15144180218491424f,0.7572090298601859f,0.6353737999392022f,0.2474096139564977f,0.7422288789915019f,0.622803961221278f,0.33662412251131973f,0.7213374105352763f,0.6052739382343718f,0.41762559367376817f,0.6960427025390038f,0.5840491586765901f,0.48977498010487086f,0.6678749971635558f,0.5604136481448803f,0.5531064412306825f,0.6381997497028097f,0.5355131596407324f,0.6081204068324845f,0.6081204068324845f,0.510273594836914f,-0.6381997497028097f,0.5531064412306825f,0.5355131596407324f,-0.5834703446932868f,0.5834703446932868f,0.5649112441145016f,-0.5194577969133418f,0.6139046819621898f,0.5943775220269016f,-0.44533730505952845f,0.6432650291944384f,0.6228039715173587f,-0.3607960637755622f,0.6700498272020832f,0.6487367951877857f,-0.26634209783642093f,0.6924894783523086f,0.6704626829969301f,-0.16356126144095376f,0.7087654730611258f,0.6862209687500687f,-0.05518046011087484f,0.7173459659174505f,0.6945285321741017f,0.05518046011087484f,0.7173459659174505f,0.6945285321741017f,0.16356126144095376f,0.7087654730611258f,0.6862209687500687f,0.26634209783642093f,0.6924894783523086f,0.6704626829969301f,0.3607960637755622f,0.6700498272020832f,0.6487367951877857f,0.44533730505952845f,0.6432650291944384f,0.6228039715173587f,0.5194577969133418f,0.6139046819621898f,0.5943775220269016f,0.5834703446932868f,0.5834703446932868f,0.5649112441145016f,0.6381997497028097f,0.5531064412306825f,0.5355131596407324f,-0.6678749971635558f,0.48977498010487086f,0.5604136481448803f,-0.6139046819621898f,0.5194577969133418f,0.5943775220269016f,-0.5497115831320021f,0.5497115831320021f,0.6289947143983134f,-0.47406528196754993f,0.5794131399677562f,0.6629800311214001f,-0.3862632181382274f,0.6069850393327315f,0.6945285367353471f,-0.28657184395651625f,0.630458065303939f,0.7213870016465339f,-0.17664798564454903f,0.6477092733342515f,0.7411262958528316f,-0.05971667872444935f,0.656883437968781f,0.7516236207068239f,0.05971667872444935f,0.656883437968781f,0.7516236207068239f,0.17664798564454903f,0.6477092733342515f,0.7411262958528316f,0.28657184395651625f,0.630458065303939f,0.7213870016465339f,0.3862632181382274f,0.6069850393327315f,0.6945285367353471f,0.47406528196754993f,0.5794131399677562f,0.6629800311214001f,0.5497115831320021f,0.5497115831320021f,0.6289947143983134f,0.6139046819621898f,0.5194577969133418f,0.5943775220269016f,0.6678749971635558f,0.48977498010487086f,0.5604136481448803f,-0.6960427025390038f,0.41762559367376817f,0.5840491586765901f,-0.6432650291944384f,0.44533730505952845f,0.6228039715173587f,-0.5794131399677562f,0.47406528196754993f,0.6629800311214001f,-0.5027855637346055f,0.5027855637346055f,0.7031453290751136f,-0.4121786421412294f,0.5299439369140309f,0.741126298744465f,-0.30748432371293205f,0.5534717734561215f,0.7740299649613938f,-0.19033914754991682f,0.5710174188505832f,0.798567540211339f,-0.06449333955004294f,0.5804400136145839f,0.8117450337074965f,0.06449333955004294f,0.5804400136145839f,0.8117450337074965f,0.19033914754991682f,0.5710174188505832f,0.798567540211339f,0.30748432371293205f,0.5534717734561215f,0.7740299649613938f,0.4121786421412294f,0.5299439369140309f,0.741126298744465f,0.5027855637346055f,0.5027855637346055f,0.7031453290751136f,0.5794131399677562f,0.47406528196754993f,0.6629800311214001f,0.6432650291944384f,0.44533730505952845f,0.6228039715173587f,0.6960427025390038f,0.41762559367376817f,0.5840491586765901f,-0.7213374105352763f,0.33662412251131973f,0.6052739382343718f,-0.6700498272020832f,0.3607960637755622f,0.6487367951877857f,-0.6069850393327315f,0.3862632181382274f,0.6945285367353471f,-0.5299439369140309f,0.4121786421412294f,0.741126298744465f,-0.43714169372759276f,0.43714169372759276f,0.7860116279101366f,-0.32796945046050996f,0.459157250328484f,0.8255971529961916f,-0.2039260537316777f,0.475827467206586f,0.8555713798749875f,-0.06926765424379594f,0.48487357321088037f,0.8718369171337464f,0.06926765424379594f,0.48487357321088037f,0.8718369171337464f,0.2039260537316777f,0.475827467206586f,0.8555713798749875f,0.32796945046050996f,0.459157250328484f,0.8255971529961916f,0.43714169372759276f,0.43714169372759276f,0.7860116279101366f,0.5299439369140309f,0.4121786421412294f,0.741126298744465f,0.6069850393327315f,0.3862632181382274f,0.6945285367353471f,0.6700498272020832f,0.3607960637755622f,0.6487367951877857f,0.7213374105352763f,0.33662412251131973f,0.6052739382343718f,-0.7422288789915019f,0.2474096139564977f,0.622803961221278f,-0.6924894783523086f,0.26634209783642093f,0.6704626829969301f,-0.630458065303939f,0.28657184395651625f,0.7213870016465339f,-0.5534717734561215f,0.30748432371293205f,0.7740299649613938f,-0.459157250328484f,0.32796945046050996f,0.8255971529961916f,-0.34633825531603923f,0.34633825531603923f,0.871836926156081f,-0.21627002438443277f,0.3604500316269047f,0.9073604858340044f,-0.07363701784259213f,0.36818506849665367f,0.926831886017828f,0.07363701784259213f,0.36818506849665367f,0.926831886017828f,0.21627002438443277f,0.3604500316269047f,0.9073604858340044f,0.34633825531603923f,0.34633825531603923f,0.871836926156081f,0.459157250328484f,0.32796945046050996f,0.8255971529961916f,0.5534717734561215f,0.30748432371293205f,0.7740299649613938f,0.630458065303939f,0.28657184395651625f,0.7213870016465339f,0.6924894783523086f,0.26634209783642093f,0.6704626829969301f,0.7422288789915019f,0.2474096139564977f,0.622803961221278f,-0.7572090298601859f,0.15144180218491424f,0.6353737999392022f,-0.7087654730611258f,0.16356126144095376f,0.6862209687500687f,-0.6477092733342515f,0.17664798564454903f,0.7411262958528316f,-0.5710174188505832f,0.19033914754991682f,0.798567540211339f,-0.475827467206586f,0.2039260537316777f,0.8555713798749875f,-0.3604500316269047f,0.21627002438443277f,0.9073604858340044f,-0.2258642730996951f,0.2258642730996951f,0.9476131385086916f,-0.07705545436012692f,0.23116635585437545f,0.9698580168634802f,0.07705545436012692f,0.23116635585437545f,0.9698580168634802f,0.2258642730996951f,0.2258642730996951f,0.9476131385086916f,0.3604500316269047f,0.21627002438443277f,0.9073604858340044f,0.475827467206586f,0.2039260537316777f,0.8555713798749875f,0.5710174188505832f,0.19033914754991682f,0.798567540211339f,0.6477092733342515f,0.17664798564454903f,0.7411262958528316f,0.7087654730611258f,0.16356126144095376f,0.6862209687500687f,0.7572090298601859f,0.15144180218491424f,0.6353737999392022f,-0.7650474387632372f,0.05100316290307633f,0.6419510057751238f,-0.7173459659174505f,0.05518046011087484f,0.6945285321741017f,-0.656883437968781f,0.05971667872444935f,0.7516236207068239f,-0.5804400136145839f,0.06449333955004294f,0.8117450337074965f,-0.48487357321088037f,0.06926765424379594f,0.8718369171337464f,-0.36818506849665367f,0.07363701784259213f,0.926831886017828f,-0.23116635585437545f,0.07705545436012692f,0.9698580168634802f,-0.07895342440795294f,0.07895342440795294f,0.9937468055538672f,0.07895342440795294f,0.07895342440795294f,0.9937468055538672f,0.23116635585437545f,0.07705545436012692f,0.9698580168634802f,0.36818506849665367f,0.07363701784259213f,0.926831886017828f,0.48487357321088037f,0.06926765424379594f,0.8718369171337464f,0.5804400136145839f,0.06449333955004294f,0.8117450337074965f,0.656883437968781f,0.05971667872444935f,0.7516236207068239f,0.7173459659174505f,0.05518046011087484f,0.6945285321741017f,0.7650474387632372f,
                0.05100316290307633f,0.6419510057751238f,-0.7650474387632372f,-0.05100316290307633f,0.6419510057751238f,-0.7173459659174505f,-0.05518046011087484f,0.6945285321741017f,-0.656883437968781f,-0.05971667872444935f,0.7516236207068239f,-0.5804400136145839f,-0.06449333955004294f,0.8117450337074965f,-0.48487357321088037f,-0.06926765424379594f,0.8718369171337464f,-0.36818506849665367f,-0.07363701784259213f,0.926831886017828f,-0.23116635585437545f,-0.07705545436012692f,0.9698580168634802f,-0.07895342440795294f,-0.07895342440795294f,0.9937468055538672f,0.07895342440795294f,-0.07895342440795294f,0.9937468055538672f,0.23116635585437545f,-0.07705545436012692f,0.9698580168634802f,0.36818506849665367f,-0.07363701784259213f,0.926831886017828f,0.48487357321088037f,-0.06926765424379594f,0.8718369171337464f,0.5804400136145839f,-0.06449333955004294f,0.8117450337074965f,0.656883437968781f,-0.05971667872444935f,0.7516236207068239f,0.7173459659174505f,-0.05518046011087484f,0.6945285321741017f,0.7650474387632372f,-0.05100316290307633f,0.6419510057751238f,
                -0.7572090298601859f,-0.15144180218491424f,0.6353737999392022f,-0.7087654730611258f,-0.16356126144095376f,0.6862209687500687f,-0.6477092733342515f,-0.17664798564454903f,0.7411262958528316f,-0.5710174188505832f,-0.19033914754991682f,0.798567540211339f,-0.475827467206586f,-0.2039260537316777f,0.8555713798749875f,-0.3604500316269047f,-0.21627002438443277f,0.9073604858340044f,-0.2258642730996951f,-0.2258642730996951f,0.9476131385086916f,-0.07705545436012692f,-0.23116635585437545f,0.9698580168634802f,0.07705545436012692f,-0.23116635585437545f,0.9698580168634802f,0.2258642730996951f,-0.2258642730996951f,0.9476131385086916f,0.3604500316269047f,-0.21627002438443277f,0.9073604858340044f,0.475827467206586f,-0.2039260537316777f,0.8555713798749875f,0.5710174188505832f,-0.19033914754991682f,0.798567540211339f,0.6477092733342515f,-0.17664798564454903f,0.7411262958528316f,0.7087654730611258f,-0.16356126144095376f,0.6862209687500687f,0.7572090298601859f,-0.15144180218491424f,0.6353737999392022f,-0.7422288789915019f,-0.2474096139564977f,0.622803961221278f,-0.6924894783523086f,-0.26634209783642093f,0.6704626829969301f,-0.630458065303939f,-0.28657184395651625f,0.7213870016465339f,-0.5534717734561215f,-0.30748432371293205f,0.7740299649613938f,-0.459157250328484f,-0.32796945046050996f,0.8255971529961916f,-0.34633825531603923f,-0.34633825531603923f,0.871836926156081f,-0.21627002438443277f,-0.3604500316269047f,0.9073604858340044f,-0.07363701784259213f,-0.36818506849665367f,0.926831886017828f,0.07363701784259213f,-0.36818506849665367f,0.926831886017828f,0.21627002438443277f,-0.3604500316269047f,0.9073604858340044f,0.34633825531603923f,-0.34633825531603923f,0.871836926156081f,0.459157250328484f,-0.32796945046050996f,0.8255971529961916f,0.5534717734561215f,-0.30748432371293205f,0.7740299649613938f,0.630458065303939f,-0.28657184395651625f,0.7213870016465339f,0.6924894783523086f,-0.26634209783642093f,0.6704626829969301f,0.7422288789915019f,-0.2474096139564977f,0.622803961221278f,-0.7213374105352763f,-0.33662412251131973f,0.6052739382343718f,-0.6700498272020832f,-0.3607960637755622f,0.6487367951877857f,-0.6069850393327315f,-0.3862632181382274f,0.6945285367353471f,-0.5299439369140309f,-0.4121786421412294f,0.741126298744465f,-0.43714169372759276f,-0.43714169372759276f,0.7860116279101366f,-0.32796945046050996f,-0.459157250328484f,0.8255971529961916f,-0.2039260537316777f,-0.475827467206586f,0.8555713798749875f,-0.06926765424379594f,-0.48487357321088037f,0.8718369171337464f,0.06926765424379594f,-0.48487357321088037f,0.8718369171337464f,0.2039260537316777f,-0.475827467206586f,0.8555713798749875f,0.32796945046050996f,-0.459157250328484f,0.8255971529961916f,0.43714169372759276f,-0.43714169372759276f,0.7860116279101366f,0.5299439369140309f,-0.4121786421412294f,0.741126298744465f,0.6069850393327315f,-0.3862632181382274f,0.6945285367353471f,0.6700498272020832f,-0.3607960637755622f,0.6487367951877857f,0.7213374105352763f,-0.33662412251131973f,0.6052739382343718f,-0.6960427025390038f,-0.41762559367376817f,0.5840491586765901f,-0.6432650291944384f,-0.44533730505952845f,0.6228039715173587f,-0.5794131399677562f,-0.47406528196754993f,0.6629800311214001f,-0.5027855637346055f,-0.5027855637346055f,0.7031453290751136f,-0.4121786421412294f,-0.5299439369140309f,0.741126298744465f,-0.30748432371293205f,-0.5534717734561215f,0.7740299649613938f,-0.19033914754991682f,-0.5710174188505832f,0.798567540211339f,-0.06449333955004294f,-0.5804400136145839f,0.8117450337074965f,0.06449333955004294f,-0.5804400136145839f,0.8117450337074965f,0.19033914754991682f,-0.5710174188505832f,0.798567540211339f,0.30748432371293205f,-0.5534717734561215f,0.7740299649613938f,0.4121786421412294f,-0.5299439369140309f,0.741126298744465f,0.5027855637346055f,-0.5027855637346055f,0.7031453290751136f,0.5794131399677562f,-0.47406528196754993f,0.6629800311214001f,0.6432650291944384f,-0.44533730505952845f,0.6228039715173587f,0.6960427025390038f,-0.41762559367376817f,0.5840491586765901f,-0.6678749971635558f,-0.48977498010487086f,0.5604136481448803f,-0.6139046819621898f,-0.5194577969133418f,0.5943775220269016f,-0.5497115831320021f,-0.5497115831320021f,0.6289947143983134f,-0.47406528196754993f,-0.5794131399677562f,0.6629800311214001f,-0.3862632181382274f,-0.6069850393327315f,0.6945285367353471f,-0.28657184395651625f,-0.630458065303939f,0.7213870016465339f,-0.17664798564454903f,-0.6477092733342515f,0.7411262958528316f,-0.05971667872444935f,-0.656883437968781f,0.7516236207068239f,0.05971667872444935f,-0.656883437968781f,0.7516236207068239f,0.17664798564454903f,-0.6477092733342515f,0.7411262958528316f,0.28657184395651625f,-0.630458065303939f,0.7213870016465339f,0.3862632181382274f,-0.6069850393327315f,0.6945285367353471f,0.47406528196754993f,-0.5794131399677562f,0.6629800311214001f,0.5497115831320021f,-0.5497115831320021f,0.6289947143983134f,0.6139046819621898f,-0.5194577969133418f,0.5943775220269016f,0.6678749971635558f,-0.48977498010487086f,0.5604136481448803f,-0.6381997497028097f,-0.5531064412306825f,0.5355131596407324f,-0.5834703446932868f,-0.5834703446932868f,0.5649112441145016f,-0.5194577969133418f,-0.6139046819621898f,0.5943775220269016f,-0.44533730505952845f,-0.6432650291944384f,0.6228039715173587f,-0.3607960637755622f,-0.6700498272020832f,0.6487367951877857f,-0.26634209783642093f,-0.6924894783523086f,0.6704626829969301f,-0.16356126144095376f,-0.7087654730611258f,0.6862209687500687f,-0.05518046011087484f,-0.7173459659174505f,0.6945285321741017f,0.05518046011087484f,-0.7173459659174505f,0.6945285321741017f,0.16356126144095376f,-0.7087654730611258f,0.6862209687500687f,0.26634209783642093f,-0.6924894783523086f,0.6704626829969301f,0.3607960637755622f,-0.6700498272020832f,0.6487367951877857f,0.44533730505952845f,-0.6432650291944384f,0.6228039715173587f,0.5194577969133418f,-0.6139046819621898f,0.5943775220269016f,0.5834703446932868f,-0.5834703446932868f,0.5649112441145016f,0.6381997497028097f,-0.5531064412306825f,0.5355131596407324f,-0.6081204068324845f,-0.6081204068324845f,0.510273594836914f,-0.5531064412306825f,-0.6381997497028097f,0.5355131596407324f,-0.48977498010487086f,-0.6678749971635558f,0.5604136481448803f,-0.41762559367376817f,-0.6960427025390038f,0.5840491586765901f,-0.33662412251131973f,-0.7213374105352763f,0.6052739382343718f,-0.2474096139564977f,-0.7422288789915019f,0.622803961221278f,-0.15144180218491424f,-0.7572090298601859f,0.6353737999392022f,-0.05100316290307633f,-0.7650474387632372f,0.6419510057751238f,0.05100316290307633f,-0.7650474387632372f,0.6419510057751238f,0.15144180218491424f,-0.7572090298601859f,0.6353737999392022f,0.2474096139564977f,-0.7422288789915019f,0.622803961221278f,0.33662412251131973f,-0.7213374105352763f,0.6052739382343718f,0.41762559367376817f,-0.6960427025390038f,0.5840491586765901f,0.48977498010487086f,-0.6678749971635558f,0.5604136481448803f,0.5531064412306825f,-0.6381997497028097f,0.5355131596407324f,0.6081204068324845f,-0.6081204068324845f,0.510273594836914f};
        short[] INDICES = new short[] {0, 1, 16, 1, 17, 16, 1, 2, 17, 2, 18, 17, 2, 3, 18, 3, 19, 18, 3, 4, 19, 4, 20, 19, 4, 5, 20, 5, 21, 20, 5, 6, 21, 6, 22, 21, 6, 7, 22, 7, 23, 22, 7, 8, 23, 8, 24, 23, 8, 9, 24, 9, 25, 24, 9, 10, 25, 10, 26, 25, 10, 11, 26, 11, 27, 26, 11, 12, 27, 12, 28, 27, 12, 13, 28, 13, 29, 28, 13, 14, 29, 14, 30, 29, 14, 15, 30, 15, 31, 30, 16, 17, 32, 17, 33, 32, 17, 18, 33, 18, 34, 33, 18, 19, 34, 19, 35, 34, 19, 20, 35, 20, 36, 35, 20, 21, 36, 21, 37, 36, 21, 22, 37, 22, 38, 37, 22, 23, 38, 23, 39, 38, 23, 24, 39, 24, 40, 39, 24, 25, 40, 25, 41, 40, 25, 26, 41, 26, 42, 41, 26, 27, 42, 27, 43, 42, 27, 28, 43, 28, 44, 43, 28, 29, 44, 29, 45, 44, 29, 30, 45, 30, 46, 45, 30, 31, 46, 31, 47, 46, 32, 33, 48, 33, 49, 48, 33, 34, 49, 34, 50, 49, 34, 35, 50, 35, 51, 50, 35, 36, 51, 36, 52, 51, 36, 37, 52, 37, 53, 52, 37, 38, 53, 38, 54, 53, 38, 39, 54, 39, 55, 54, 39, 40, 55, 40, 56, 55, 40, 41, 56, 41, 57, 56, 41, 42, 57, 42, 58, 57, 42, 43, 58, 43, 59, 58, 43, 44, 59, 44, 60, 59, 44, 45, 60, 45, 61, 60, 45, 46, 61, 46, 62, 61, 46, 47, 62, 47, 63, 62, 48, 49, 64, 49, 65, 64, 49, 50, 65, 50, 66, 65, 50, 51, 66, 51, 67, 66, 51, 52, 67, 52, 68, 67, 52, 53, 68, 53, 69, 68, 53, 54, 69, 54, 70, 69, 54, 55, 70, 55, 71, 70, 55, 56, 71, 56, 72, 71, 56, 57, 72, 57, 73, 72, 57, 58, 73, 58, 74, 73, 58, 59, 74, 59, 75, 74, 59, 60, 75, 60, 76, 75, 60, 61, 76, 61, 77, 76, 61, 62, 77, 62, 78, 77, 62, 63, 78, 63, 79, 78, 64, 65, 80, 65, 81, 80, 65, 66, 81, 66, 82, 81, 66, 67, 82, 67, 83, 82, 67, 68, 83, 68, 84, 83, 68, 69, 84, 69, 85, 84, 69, 70, 85, 70, 86, 85, 70, 71, 86, 71, 87, 86, 71, 72, 87, 72, 88, 87, 72, 73, 88, 73, 89, 88, 73, 74, 89, 74, 90, 89, 74, 75, 90, 75, 91, 90, 75, 76, 91, 76, 92, 91, 76, 77, 92, 77, 93, 92, 77, 78, 93, 78, 94, 93, 78, 79, 94, 79, 95, 94, 80, 81, 96, 81, 97, 96, 81, 82, 97, 82, 98, 97, 82, 83, 98, 83, 99, 98, 83, 84, 99, 84, 100, 99, 84, 85, 100, 85, 101, 100, 85, 86, 101, 86, 102, 101, 86, 87, 102, 87, 103, 102, 87, 88, 103, 88, 104, 103, 88, 89, 104, 89, 105, 104, 89, 90, 105, 90, 106, 105, 90, 91, 106, 91, 107, 106, 91, 92, 107, 92, 108, 107, 92, 93, 108, 93, 109, 108, 93, 94, 109, 94, 110, 109, 94, 95, 110, 95, 111, 110, 96, 97, 112, 97, 113, 112, 97, 98, 113, 98, 114, 113, 98, 99, 114, 99, 115, 114, 99, 100, 115, 100, 116, 115, 100, 101, 116, 101, 117, 116, 101, 102, 117, 102, 118, 117, 102, 103, 118, 103, 119, 118, 103, 104, 119, 104, 120, 119, 104, 105, 120, 105, 121, 120, 105, 106, 121, 106, 122, 121, 106, 107, 122, 107, 123, 122, 107, 108, 123, 108, 124, 123, 108, 109, 124, 109, 125, 124, 109, 110, 125, 110, 126, 125, 110, 111, 126, 111, 127, 126, 112, 113, 128, 113, 129, 128, 113, 114, 129, 114, 130, 129, 114, 115, 130, 115, 131, 130, 115, 116, 131, 116, 132, 131, 116, 117, 132, 117, 133, 132, 117, 118, 133, 118, 134, 133, 118, 119, 134, 119, 135, 134, 119, 120, 135, 120, 136, 135, 120, 121, 136, 121, 137, 136, 121, 122, 137, 122, 138, 137, 122, 123, 138, 123, 139, 138, 123, 124, 139, 124, 140, 139, 124, 125, 140, 125, 141, 140, 125, 126, 141, 126, 142, 141, 126, 127, 142, 127, 143, 142, 128, 129, 144, 129, 145, 144, 129, 130, 145, 130, 146, 145, 130, 131, 146, 131, 147, 146, 131, 132, 147, 132, 148, 147, 132, 133, 148, 133, 149, 148, 133, 134, 149, 134, 150, 149, 134, 135, 150, 135, 151, 150, 135, 136, 151, 136, 152, 151, 136, 137, 152, 137, 153, 152, 137, 138, 153, 138, 154, 153, 138, 139, 154, 139, 155, 154, 139, 140, 155, 140, 156, 155, 140, 141, 156, 141, 157, 156, 141, 142, 157, 142, 158, 157, 142, 143, 158, 143, 159, 158, 144, 145, 160, 145, 161, 160, 145, 146, 161, 146, 162, 161, 146, 147, 162, 147, 163, 162, 147, 148, 163, 148, 164, 163, 148, 149, 164, 149, 165, 164, 149, 150, 165, 150, 166, 165, 150, 151, 166, 151, 167, 166, 151, 152, 167, 152, 168, 167, 152, 153, 168, 153, 169, 168, 153, 154, 169, 154, 170, 169, 154, 155, 170, 155, 171, 170, 155, 156, 171, 156, 172, 171, 156, 157, 172, 157, 173, 172, 157, 158, 173, 158, 174, 173, 158, 159, 174, 159, 175, 174, 160, 161, 176, 161, 177, 176, 161, 162, 177, 162, 178, 177, 162, 163, 178, 163, 179, 178, 163, 164, 179, 164, 180, 179, 164, 165, 180, 165, 181, 180, 165, 166, 181, 166, 182, 181, 166, 167, 182, 167, 183, 182, 167, 168, 183, 168, 184, 183, 168, 169, 184, 169, 185, 184, 169, 170, 185, 170, 186, 185, 170, 171, 186, 171, 187, 186, 171, 172, 187, 172, 188, 187, 172, 173, 188, 173, 189, 188, 173, 174, 189, 174, 190, 189, 174, 175, 190, 175, 191, 190, 176, 177, 192, 177, 193, 192, 177, 178, 193, 178, 194, 193, 178, 179, 194, 179, 195, 194, 179, 180, 195, 180, 196, 195, 180, 181, 196, 181, 197, 196, 181, 182, 197, 182, 198, 197, 182, 183, 198, 183, 199, 198, 183, 184, 199, 184, 200, 199, 184, 185, 200, 185, 201, 200, 185, 186, 201, 186, 202, 201, 186, 187, 202, 187, 203, 202, 187, 188, 203, 188, 204, 203, 188, 189, 204, 189, 205, 204, 189, 190, 205, 190, 206, 205, 190, 191, 206, 191, 207, 206, 192, 193, 208, 193, 209, 208, 193, 194, 209, 194, 210, 209, 194, 195, 210, 195, 211, 210, 195, 196, 211, 196, 212, 211, 196, 197, 212, 197, 213, 212, 197, 198, 213, 198, 214, 213, 198, 199, 214, 199, 215, 214, 199, 200, 215, 200, 216, 215, 200, 201, 216, 201, 217, 216, 201, 202, 217, 202, 218, 217, 202, 203, 218, 203, 219, 218, 203, 204, 219, 204, 220, 219, 204, 205, 220, 205, 221, 220, 205, 206, 221, 206, 222, 221, 206, 207, 222, 207, 223, 222, 208, 209, 224, 209, 225, 224, 209, 210, 225, 210, 226, 225, 210, 211, 226, 211, 227, 226, 211, 212, 227, 212, 228, 227, 212, 213, 228, 213, 229, 228, 213, 214, 229, 214, 230, 229, 214, 215, 230, 215, 231, 230, 215, 216, 231, 216, 232, 231, 216, 217, 232, 217, 233, 232, 217, 218, 233, 218, 234, 233, 218, 219, 234, 219, 235, 234, 219, 220, 235, 220, 236, 235, 220, 221, 236, 221, 237, 236, 221, 222, 237, 222, 238, 237, 222, 223, 238, 223, 239, 238, 224, 225, 240, 225, 241, 240, 225, 226, 241, 226, 242, 241, 226, 227, 242, 227, 243, 242, 227, 228, 243, 228, 244, 243, 228, 229, 244, 229, 245, 244, 229, 230, 245, 230, 246, 245, 230, 231, 246, 231, 247, 246, 231, 232, 247, 232, 248, 247, 232, 233, 248, 233, 249, 248, 233, 234, 249, 234, 250, 249, 234, 235, 250, 235, 251, 250, 235, 236, 251, 236, 252, 251, 236, 237, 252, 237, 253, 252, 237, 238, 253, 238, 254, 253, 238, 239, 254, 239, 255, 254};

        setData(VERTICES, INDICES);
    }
}
