
public class Tomograph {
    private final float alfa;
    private final int steps;
    private final int beta;
    private final int radius;

    private final Emitter emitter;
    private final Detector detector;

    private class Emitter {
        private int computePosX(int i) {
            return (int) (Math.ceil(Math.cos((alfa * Math.PI)/180 * i) * radius));
        }

        private int computePosY(int i) {
            return (int) (Math.ceil(Math.sin((alfa * Math.PI)/180 * i) * radius));
        }
    }

    private class Detector {
        private final int sensorsCount;

        private Detector(int sensorsCount) {
            this.sensorsCount = sensorsCount + (1 - sensorsCount%2);
        }

        private int getSensorsCount() {
            return sensorsCount;
        }

        private int computeSensorPosX(int i, int sensorIndex) {
            return (int) (Math.ceil(Math.cos((i * alfa + 180 - beta/2 + sensorIndex*beta/(sensorsCount - 1)) * Math.PI/180) * radius));
        }

        private int computeSensorPosY(int i, int sensorIndex) {
            return (int) (Math.ceil(Math.sin((i * alfa + 180 - beta/2 + sensorIndex*beta/(sensorsCount - 1)) * Math.PI/180) * radius));
        }
    }

    public Tomograph(float alfa, int beta, int detectorCount, int radius) {
        this.alfa = alfa;
        this.steps = computeSteps(alfa);
        this.beta = beta;
        this.radius = radius;
        this.emitter = new Emitter();
        this.detector = new Detector(detectorCount);
    }

    // TODO normalizacja alfy (czy musimy na całych 180 stopniach?, czy musi być podzielne)
    private int computeSteps(float angle) {
        double count = Math.ceil(360 / angle);
        return (int) (count /*/ 2*/) + 1;       // 0 - 180 deg.
    }


    public int getSteps() {
        return steps;
    }

    public int getDetectorsSensorsCount() {
        return detector.getSensorsCount();
    }

    public int getEmitterPosX(int i) {
        return emitter.computePosX(i);
    }

    public int getEmitterPosY(int i) {
        return emitter.computePosY(i);
    }

    public int getDetectorsSensorPosX(int i, int sensorIndex) {
        return detector.computeSensorPosX(i, sensorIndex);
    }

    public int getDetectorsSensorPosY(int i, int sensorIndex) {
        return detector.computeSensorPosY(i, sensorIndex);
    }
}
