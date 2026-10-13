#!/usr/bin/env python3
"""Experimental helper for reviewing chart screenshots.

This script does NOT validate trade outcomes or infer a reliable BUY/SELL signal.
The screenshot filename supplies the market/action label; image measurements are
only coarse visual candidates and must be manually reviewed before app use.
"""
import argparse
import csv
import json
import re
from pathlib import Path

import cv2
import numpy as np

IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".bmp", ".webp"}
FILENAME_RE = re.compile(r"^(\d{1,3})_([A-Za-z0-9-]+)_(BUY|SELL)$", re.IGNORECASE)


class CandleAnalyzer:
    def analyze_image(self, image_path: Path):
        match = FILENAME_RE.fullmatch(image_path.stem)
        if not match:
            print(f"Skipping unexpected filename (expected ID_MARKET_BUY/SELL): {image_path.name}")
            return None
        image = cv2.imread(str(image_path))
        if image is None or image.size == 0:
            print(f"Could not decode image: {image_path}")
            return None

        pattern_id, market, action = match.groups()
        height, width = image.shape[:2]
        # These regions are merely coarse image summaries, not candle segmentation.
        y1, y2 = int(height * .2), int(height * .8)
        x1, x2 = int(width * .3), width
        if y2 <= y1 or x2 <= x1:
            print(f"Image is too small to inspect: {image_path}")
            return None
        roi = image[y1:y2, x1:x2]
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        green = cv2.inRange(hsv, np.array([35, 70, 50]), np.array([90, 255, 255]))
        red1 = cv2.inRange(hsv, np.array([0, 70, 50]), np.array([10, 255, 255]))
        red2 = cv2.inRange(hsv, np.array([170, 70, 50]), np.array([180, 255, 255]))
        green_pixels = int(cv2.countNonZero(green))
        red_pixels = int(cv2.countNonZero(red1) + cv2.countNonZero(red2))
        total = green_pixels + red_pixels
        # Do not invent candle body/wick sizes or a confidence percentage.
        dominant_color = "GREEN" if green_pixels > red_pixels else "RED" if red_pixels > green_pixels else "UNKNOWN"
        return {
            "id": int(pattern_id),
            "market": market,
            "action": action.upper(),
            "image": image_path.name,
            "coarse_color_candidate": dominant_color if total else "UNKNOWN",
            "red_pixels": red_pixels,
            "green_pixels": green_pixels,
            "review_required": True,
            "confidence": None,
            "note": "Pixel counts are not candle segmentation and must not be used as a trade signal."
        }

    def process_directory(self, directory: Path):
        files = sorted(p for p in directory.iterdir() if p.is_file() and p.suffix.lower() in IMAGE_EXTENSIONS)
        print(f"Found {len(files)} supported image(s)")
        records = []
        seen_ids = set()
        for image_path in files:
            record = self.analyze_image(image_path)
            if record is None:
                continue
            if record["id"] in seen_ids:
                print(f"Warning: duplicate pattern ID {record['id']} ({image_path.name})")
            seen_ids.add(record["id"])
            records.append(record)
        ids = {r["id"] for r in records}
        missing = sorted(set(range(1, 97)) - ids)
        if missing:
            print("Missing pattern IDs: " + ", ".join(map(str, missing)))
        if len(records) != 96:
            print(f"WARNING: expected 96 valid screenshots, found {len(records)}. No missing screenshots were fabricated.")
        return records


def main():
    parser = argparse.ArgumentParser(description="Inspect screenshot image colors for manual review (not a signal engine)")
    parser.add_argument("directory", type=Path, help="Folder containing screenshots named ID_MARKET_BUY/SELL")
    parser.add_argument("--output", "-o", default="patterns_data", help="Output filename without extension")
    parser.add_argument("--format", "-f", choices=["json", "csv", "all"], default="all")
    args = parser.parse_args()
    if not args.directory.is_dir():
        parser.error(f"Directory not found: {args.directory}")

    records = CandleAnalyzer().process_directory(args.directory)
    if args.format in ("json", "all"):
        Path(f"{args.output}.json").write_text(json.dumps(records, indent=2), encoding="utf-8")
        print(f"JSON saved: {args.output}.json")
    if args.format in ("csv", "all"):
        fields = ["id", "market", "action", "image", "coarse_color_candidate", "red_pixels", "green_pixels", "review_required", "confidence", "note"]
        with open(f"{args.output}.csv", "w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields)
            writer.writeheader()
            writer.writerows(records)
        print(f"CSV saved: {args.output}.csv")
    print(f"Review records produced: {len(records)}. This is not a validated pattern database.")


if __name__ == "__main__":
    main()
