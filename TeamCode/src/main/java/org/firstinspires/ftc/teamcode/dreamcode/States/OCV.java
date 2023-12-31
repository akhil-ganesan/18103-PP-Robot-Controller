package org.firstinspires.ftc.teamcode.dreamcode.States;

import static java.lang.Thread.sleep;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class OCV implements State {

    OpenCvWebcam webcam;
    NavigationPipeline pipeline;

    public OCV (OpenCvWebcam webcam) {
        this.webcam = webcam;
        pipeline = new NavigationPipeline();
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                webcam.startStreaming(320,240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {}
        });



    }

    public int getCb() {
        return pipeline.getCb();
    }

    public int getCr() {
        return pipeline.getCr();
    }

    public int getY() {
        return pipeline.getY();
    }

    public int getDiff() {return getCb() - getCr();}

    public NavigationPipeline.NavPos getAnalysis() {return pipeline.getAnalysis();}

    @Override
    public void update(double dt, Telemetry telemetry) {
        /*telemetry.addData("Vision Cb: ", pipeline.getCb());
        telemetry.addData("Vision Cr: ", pipeline.getCr());
        telemetry.addData("Vision Y: ", pipeline.getY());
        try {
            sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public static class NavigationPipeline extends OpenCvPipeline
    {
        public enum NavPos
        {
            LEFT(0, "Left"),
            CENTER(1, "Center"),
            RIGHT(2, "Right");

            private final int i;
            private final String name;

            NavPos(int i, String name) {
                this.i = i;
                this.name = name;
            }

            public int getI() {
                return i;
            }

            public String getName() {
                return name;
            }

        }

        /*
         * Some color constants
         */
        static final Scalar BLUE = new Scalar(0, 0, 255);
        static final Scalar GREEN = new Scalar(0, 255, 0);
        static final Scalar RED = new Scalar(255, 0, 0);
        static final Scalar BLACK = new Scalar(0, 0, 0);


        /*
         * The core values which define the location and size of the sample regions
         */
        static final Point TOPLEFT_ANCHOR_POINT = new Point(125,125);
        static final int REGION_WIDTH = 50;
        static final int REGION_HEIGHT = 50;

        /*
         * Points which actually define the sample region rectangles, derived from above values
         *
         * Example of how points A and B work to define a rectangle
         *
         *   ------------------------------------
         *   | (0,0) Point A                    |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                  Point B (70,50) |
         *   ------------------------------------
         *
         */
        Point region1_pointA = new Point(
                TOPLEFT_ANCHOR_POINT.x,
                TOPLEFT_ANCHOR_POINT.y);
        Point region1_pointB = new Point(
                TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

        /*
         * Working variables
         */
        Mat region1_Cb;
        Mat region1_Cr;
        Mat region1_Y;
        Mat YCrCb = new Mat();
        Mat Cb = new Mat();
        Mat Cr = new Mat();
        Mat Y = new Mat();
        int avgCb;
        int avgCr;
        int aveY;

        // Volatile since accessed by OpMode thread w/o synchronization
        private volatile NavPos position = NavPos.LEFT;

        /*
         * This function takes the RGB frame, converts to YCrCb,
         * and extracts the Cb channel to the 'Cb' variable
         */
        void inputToCb(Mat input)
        {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cb, 2);
        }

        void inputToCr(Mat input)
        {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cr, 1);
        }
        void inputToY(Mat input)
        {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Y, 0);
        }


        @Override
        public void init(Mat firstFrame)
        {
            /*
             * We need to call this in order to make sure the 'Cb'
             * object is initialized, so that the submats we make
             * will still be linked to it on subsequent frames. (If
             * the object were to only be initialized in processFrame,
             * then the submats would become delinked because the backing
             * buffer would be re-allocated the first time a real frame
             * was crunched)
             */
            inputToCr(firstFrame);
            inputToCb(firstFrame);
            inputToY(firstFrame);

            /*
             * Submats are a persistent reference to a region of the parent
             * buffer. Any changes to the child affect the parent, and the
             * reverse also holds true.
             */
            region1_Cb = Cb.submat(new Rect(region1_pointA, region1_pointB));
            region1_Cr = Cr.submat(new Rect(region1_pointA, region1_pointB));
            region1_Y = Y.submat(new Rect(region1_pointA, region1_pointB));
        }

        @Override
        public Mat processFrame(Mat input)
        {
            /*
             * Overview of what we're doing:
             *
             * We first convert to YCrCb color space, from RGB color space.
             * Why do we do this? Well, in the RGB color space, chroma and
             * luma are intertwined. In YCrCb, chroma and luma are separated.
             * YCrCb is a 3-channel color space, just like RGB. YCrCb's 3 channels
             * are Y, the luma channel (which essentially just a B&W image), the
             * Cr channel, which records the difference from red, and the Cb channel,
             * which records the difference from blue. Because chroma and luma are
             * not related in YCrCb, vision code written to look for certain values
             * in the Cr/Cb channels will not be severely affected by differing
             * light intensity, since that difference would most likely just be
             * reflected in the Y channel.
             *
             * After we've converted to YCrCb, we extract just the 2nd channel, the
             * Cb channel. We do this because stones are bright yellow and contrast
             * STRONGLY on the Cb channel against everything else, including SkyStones
             * (because SkyStones have a black label).
             *
             * We then take the average pixel value of 3 different regions on that Cb
             * channel, one positioned over each stone. The brightest of the 3 regions
             * is where we assume the SkyStone to be, since the normal stones show up
             * extremely darkly.
             *
             * We also draw rectangles on the screen showing where the sample regions
             * are, as well as drawing a solid rectangle over top the sample region
             * we believe is on top of the SkyStone.
             *
             * In order for this whole process to work correctly, each sample region
             * should be positioned in the center of each of the first 3 stones, and
             * be small enough such that only the stone is sampled, and not any of the
             * surroundings.
             */

            /*
             * Get the Cb channel of the input frame after conversion to YCrCb
             */
            inputToCr(input);
            inputToCb(input);
            inputToY(input);

            /*
             * Compute the average pixel value of each submat region. We're
             * taking the average of a single channel buffer, so the value
             * we need is at index 0. We could have also taken the average
             * pixel value of the 3-channel image, and referenced the value
             * at index 2 here.
             */
            avgCr = (int) Core.mean(region1_Cr).val[0];
            avgCb = (int) Core.mean(region1_Cb).val[0];
            aveY = (int) Core.mean(region1_Y).val[0];

            /*
             * Draw a rectangle showing sample region 1 on the screen.
             * Simply a visual aid. Serves no functional purpose.
             */
//            Imgproc.rectangle(
//                    input, // Buffer to draw on
//                    region1_pointA, // First point which defines the rectangle
//                    region1_pointB, // Second point which defines the rectangle
//                    RED, // The color the rectangle is drawn in
//                    2); // Thickness of the rectangle lines
            /*
             * Find the difference between the Cr and Cb values
             */

            switch(getCrCbDiff()) {
                case -1: position = NavPos.LEFT; Imgproc.rectangle(input, region1_pointA, region1_pointB, RED, 2); break;
                case 1: position = NavPos.RIGHT; Imgproc.rectangle(input, region1_pointA, region1_pointB, BLUE, 2); break;
                case 0: position = NavPos.CENTER; Imgproc.rectangle(input, region1_pointA, region1_pointB, BLACK, 2); break;
                default: position = NavPos.LEFT; Imgproc.rectangle(input, region1_pointA, region1_pointB, GREEN, 2); break;
            }
            /*
             * Render the 'input' buffer to the viewport. But note this is not
             * simply rendering the raw camera feed, because we called functions
             * to add some annotations to this buffer earlier up.
             */
            return input;
        }

        /*
         * Call this from the OpMode thread to obtain the latest analysis
         */
        public NavPos getAnalysis()
        {
            return position;
        }

        public int getCr() {return avgCr;}
        public int getCb() {return avgCb;}
        public int getY() {return aveY;}

        public int getCrCbDiff() {
            int d = getCb()- getCr();
            if(d < -15) return -1;
            if(d > 15) return 1;
            return 0;
        }

        public String getParkPosition(){
            switch(getCrCbDiff()){
                case -1: return "left";
                case 1: return "right";
                case 0: return "center";
                default: return "Nothing and therefore left";
            }
        }


    }

}
