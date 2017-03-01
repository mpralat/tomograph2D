public class Tomograph {
    private final int alfa;
    private final int steps;
    private final int beta;
    private final int radius;

    private final Emitter emitter;
    private final Detector detector;

    private class Emitter {
        private int computePosX(int i) {
            return (int) (Math.cos(alfa * i) * radius);
        }

        private int computePosY(int i) {
            return (int) (Math.cos(alfa * i) * radius);
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

        private int computeSensorPosX(int sensorIndex) {
            return (int) (Math.cos(alfa + 180 - beta/2 + sensorIndex*beta/sensorsCount) * radius);
        }

        private int computeSensorPosY(int sensorIndex) {
            return (int) (Math.sin(alfa + 180 - beta/2 + sensorIndex*beta/sensorsCount) * radius);
        }
    }

    public Tomograph(int alfa, int beta, int detectorCount, int radius) {
        this.alfa = alfa;
        this.steps = computeSteps(alfa);
        this.beta = beta;
        this.radius = radius;
        this.emitter = new Emitter();
        this.detector = new Detector(detectorCount);
    }

    // TODO normalizacja alfy (czy musimy na całych 360 stopniach?, czy musi być podzielne)
    private int computeSteps(int angle) {
        double count = Math.ceil(360 / angle);
        return (int) count;
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

    public int getDetectorsSensorPosX(int i) {
        return detector.computeSensorPosX(i);
    }

    public int getDetectorsSensorPosY(int i) {
        return detector.computeSensorPosY(i);
    }
}
