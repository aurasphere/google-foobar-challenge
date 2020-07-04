import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Solution {

	public static int solution(int[] dimensions, int[] yourPosition, int[] guardPosition, int distance) {
		// A Set would suit better the reachable points but in Java you can't
		// get elements from it, so we use a Map instead. Performance-wise this
		// shouldn't be a problem since HashSet uses internally an HashMap exactly the
		// same way we're doing it here.
		Map<RelativePosition, RelativePosition> reacheablePoints = new HashMap<>();

		// Mirrored points if they were in the same room, so we can just add an offset.
		int[] yourMirroredPosition = { dimensions[0] - yourPosition[0], dimensions[1] - yourPosition[1] };
		int[] guardMirroredPosition = { dimensions[0] - guardPosition[0], dimensions[1] - guardPosition[1] };

		// Counts the rooms to generate on each axis. We add an extra one on each side
		// just to be sure to include all reachable points.
		int halfXRooms = (distance + yourPosition[0]) / dimensions[0] + 2;
		int halfYRooms = (distance + yourPosition[1]) / dimensions[1] + 2;

		// Gets the points in all the rooms.
		for (int i = -halfXRooms; i < halfXRooms; i++) {
			int yourPositionRelativeX = i % 2 == 0 ? yourPosition[0] : yourMirroredPosition[0];
			int guardPositionRelativeX = i % 2 == 0 ? guardPosition[0] : guardMirroredPosition[0];

			for (int j = -halfYRooms; j < halfYRooms; j++) {
				int yourPositionRelativeY = j % 2 == 0 ? yourPosition[1] : yourMirroredPosition[1];
				int guardPositionRelativeY = j % 2 == 0 ? guardPosition[1] : guardMirroredPosition[1];

				// Builds the current room boundary.
				int[] roomLeftTopBoundary = { dimensions[0] * i, dimensions[1] * j };

				// We get the position of people in room and we add them to the reachable
				// points if there's nothing on the same direction or if there's something on
				// the same direction but it's further away than the current found point.

				int[] yourCoordsInRoom = { roomLeftTopBoundary[0] + yourPositionRelativeX,
						roomLeftTopBoundary[1] - yourPositionRelativeY };
				RelativePosition yourPositionInRoom = new RelativePosition(yourCoordsInRoom, yourPosition, false);
				RelativePosition yourBlockingPoint = reacheablePoints.get(yourPositionInRoom);
				if (yourBlockingPoint == null || yourBlockingPoint.distance > yourPositionInRoom.distance) {
					reacheablePoints.put(yourPositionInRoom, yourPositionInRoom);
				}

				int[] guardCoordsInRoom = { roomLeftTopBoundary[0] + guardPositionRelativeX,
						roomLeftTopBoundary[1] - guardPositionRelativeY };
				RelativePosition guardPositionInRoom = new RelativePosition(guardCoordsInRoom, yourPosition, true);
				RelativePosition guardBlockingPoint = reacheablePoints.get(guardPositionInRoom);
				if (guardBlockingPoint == null || guardBlockingPoint.distance > guardPositionInRoom.distance) {
					reacheablePoints.put(guardPositionInRoom, guardPositionInRoom);
				}
			}
		}

		// Filters the reachable points by removing the ones that are not in range and
		// the ones that are not guards.
		return (int) reacheablePoints.values().stream().filter(p -> p.distance <= distance).filter(p -> p.isGuard)
				.count();
	}

	private static class RelativePosition {
		private boolean isGuard;
		private double distance;
		private int[] bearing;

		public RelativePosition(int[] coords, int[] origin, boolean isGuard) {
			this.isGuard = isGuard;

			// Gets a direction bearing vector.
			this.bearing = new int[] { coords[0] - origin[0], coords[1] - origin[1] };

			// Calculates the distance
			this.distance = Math.hypot(bearing[0], bearing[1]);

			// Simplifies the direction (e.g. pointing to [1, 2] is the same of [2, 4]).
			int gcd = gcd(this.bearing[0], this.bearing[1]);
			if (gcd != 0) {
				this.bearing[0] /= gcd;
				this.bearing[1] /= gcd;
			}
		}

		// Greatest common divisor
		private int gcd(int a, int b) {
			if (b == 0)
				return Math.abs(a);
			return gcd(b, a % b);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(bearing);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RelativePosition other = (RelativePosition) obj;
			if (!Arrays.equals(bearing, other.bearing))
				return false;
			return true;
		}
	}

	public static void main(String... args) {
		int[] dimensionsOne = { 3, 2 }, yourPositionOne = { 1, 1 }, guardPositionOne = { 2, 1 };
		int distanceOne = 4, outputOne = 7;
		int[] dimensionsTwo = { 300, 275 }, yourPositionTwo = { 150, 150 }, guardPositionTwo = { 185, 100 };
		int distanceTwo = 500, outputTwo = 9;

		assert solution(dimensionsOne, yourPositionOne, guardPositionOne, distanceOne) == outputOne;
		assert solution(dimensionsTwo, yourPositionTwo, guardPositionTwo, distanceTwo) == outputTwo;
	}
}