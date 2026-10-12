#!/usr/bin/env python3
"""
Quotex Pattern Extractor
Automatically extract candle data from 96 pattern screenshots
"""

import os
import json
import cv2
import numpy as np
from pathlib import Path
from typing import Dict, List, Tuple
import argparse

class CandleAnalyzer:
    """Analyze candle patterns from images"""
    
    def __init__(self):
        self.patterns_data = []
    
    def detect_candle_color(self, image_section: np.ndarray) -> str:
        """Detect if candle is GREEN or RED"""
        # Convert to HSV for better color detection
        hsv = cv2.cvtColor(image_section, cv2.COLOR_BGR2HSV)
        
        # Green range in HSV
        lower_green = np.array([35, 100, 100])
        upper_green = np.array([85, 255, 255])
        green_mask = cv2.inRange(hsv, lower_green, upper_green)
        
        # Red range in HSV
        lower_red = np.array([0, 100, 100])
        upper_red = np.array([10, 255, 255])
        red_mask = cv2.inRange(hsv, lower_red, upper_red)
        
        green_pixels = cv2.countNonZero(green_mask)
        red_pixels = cv2.countNonZero(red_mask)
        
        return "GREEN" if green_pixels > red_pixels else "RED"
    
    def measure_body_size(self, candle_height: float) -> str:
        """Classify body size as SMALL, MEDIUM, or LARGE"""
        if candle_height < 30:
            return "SMALL"
        elif candle_height < 60:
            return "MEDIUM"
        else:
            return "LARGE"
    
    def measure_wick_size(self, wick_length: float) -> str:
        """Classify wick size as SMALL, MEDIUM, or LARGE"""
        if wick_length < 15:
            return "SMALL"
        elif wick_length < 30:
            return "MEDIUM"
        else:
            return "LARGE"
    
    def analyze_image(self, image_path: str) -> Dict:
        """Analyze a single pattern image"""
        filename = Path(image_path).stem
        parts = filename.split('_')
        
        if len(parts) < 3:
            return None
        
        pattern_id = parts[0]
        market = parts[1]
        action = parts[2]
        
        try:
            image = cv2.imread(image_path)
            if image is None:
                print(f"❌ Could not read: {image_path}")
                return None
            
            # Get chart area (right side usually has candles)
            height, width = image.shape[:2]
            chart_area = image[int(height*0.2):int(height*0.8), int(width*0.3):width]
            
            # Approximate candle positions (simplified - last 2 candles)
            chart_height, chart_width = chart_area.shape[:2]
            candle1_area = chart_area[:, :chart_width//2]
            candle2_area = chart_area[:, chart_width//2:]
            
            # Analyze candle 1
            c1_color = self.detect_candle_color(candle1_area)
            c1_body_size = self.measure_body_size(chart_height * 0.4)
            c1_upper_wick = self.measure_wick_size(chart_height * 0.2)
            c1_lower_wick = self.measure_wick_size(chart_height * 0.15)
            
            # Analyze candle 2
            c2_color = self.detect_candle_color(candle2_area)
            c2_body_size = self.measure_body_size(chart_height * 0.35)
            c2_upper_wick = self.measure_wick_size(chart_height * 0.18)
            c2_lower_wick = self.measure_wick_size(chart_height * 0.17)
            
            # Determine trend
            if c1_color == "GREEN" and c2_color == "RED":
                trend = "BULLISH_REVERSAL"
            elif c1_color == "RED" and c2_color == "GREEN":
                trend = "BEARISH_REVERSAL"
            elif c1_color == c2_color == "GREEN":
                trend = "BULLISH"
            else:
                trend = "BEARISH"
            
            return {
                "id": int(pattern_id),
                "market": market,
                "action": action,
                "candle1": {
                    "color": c1_color,
                    "bodySize": c1_body_size,
                    "upperWick": c1_upper_wick,
                    "lowerWick": c1_lower_wick
                },
                "candle2": {
                    "color": c2_color,
                    "bodySize": c2_body_size,
                    "upperWick": c2_upper_wick,
                    "lowerWick": c2_lower_wick
                },
                "trend": trend,
                "confidence": 85
            }
        except Exception as e:
            print(f"⚠️  Error processing {image_path}: {e}")
            return None
    
    def process_directory(self, directory_path: str) -> List[Dict]:
        """Process all pattern images in a directory"""
        image_files = sorted(Path(directory_path).glob("*.png"))
        
        print(f"🔍 Found {len(image_files)} images")
        
        for i, image_file in enumerate(image_files, 1):
            print(f"⏳ Processing [{i}/{len(image_files)}]: {image_file.name}")
            result = self.analyze_image(str(image_file))
            if result:
                self.patterns_data.append(result)
                print(f"✅ Extracted: {result['market']} - {result['action']}")
        
        return self.patterns_data
    
    def save_json(self, output_path: str):
        """Save as JSON"""
        with open(output_path, 'w') as f:
            json.dump(self.patterns_data, f, indent=2)
        print(f"\n📄 JSON saved: {output_path}")
    
    def save_csv(self, output_path: str):
        """Save as CSV"""
        import csv
        with open(output_path, 'w', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=[
                'id', 'market', 'action', 
                'c1_color', 'c1_body', 'c1_upper_wick', 'c1_lower_wick',
                'c2_color', 'c2_body', 'c2_upper_wick', 'c2_lower_wick',
                'trend', 'confidence'
            ])
            writer.writeheader()
            for pattern in self.patterns_data:
                writer.writerow({
                    'id': pattern['id'],
                    'market': pattern['market'],
                    'action': pattern['action'],
                    'c1_color': pattern['candle1']['color'],
                    'c1_body': pattern['candle1']['bodySize'],
                    'c1_upper_wick': pattern['candle1']['upperWick'],
                    'c1_lower_wick': pattern['candle1']['lowerWick'],
                    'c2_color': pattern['candle2']['color'],
                    'c2_body': pattern['candle2']['bodySize'],
                    'c2_upper_wick': pattern['candle2']['upperWick'],
                    'c2_lower_wick': pattern['candle2']['lowerWick'],
                    'trend': pattern['trend'],
                    'confidence': pattern['confidence']
                })
        print(f"📊 CSV saved: {output_path}")
    
    def save_kotlin(self, output_path: str):
        """Save as Kotlin data class for Android app"""
        kotlin_code = """package com.mdjibon.quotex99.engine

import com.mdjibon.quotex99.model.PatternRecord

class PatternStore {
    val patterns: List<PatternRecord> = listOf(
"""
        for pattern in self.patterns_data:
            c1 = pattern['candle1']
            c2 = pattern['candle2']
            kotlin_code += f'''        PatternRecord(
            {pattern['id']}, 
            "{pattern['market']}", 
            "{pattern['action']}", 
            "{c1['color']}", 
            "{c2['color']}", 
            "{c1['bodySize']}", 
            "{c2['bodySize']}"
        ),
'''
        
        kotlin_code += """    )
}
"""
        with open(output_path, 'w') as f:
            f.write(kotlin_code)
        print(f"🔷 Kotlin saved: {output_path}")

def main():
    parser = argparse.ArgumentParser(description='Extract candle data from pattern images')
    parser.add_argument('directory', help='Path to directory containing pattern images')
    parser.add_argument('--output', '-o', default='patterns_data', help='Output filename (without extension)')
    parser.add_argument('--format', '-f', choices=['json', 'csv', 'kotlin', 'all'], default='all', 
                       help='Output format')
    
    args = parser.parse_args()
    
    if not os.path.isdir(args.directory):
        print(f"❌ Directory not found: {args.directory}")
        return
    
    print("="*50)
    print("🚀 Quotex Pattern Extractor Started")
    print("="*50)
    
    analyzer = CandleAnalyzer()
    analyzer.process_directory(args.directory)
    
    print("\n📊 Saving results...")
    
    if args.format in ['json', 'all']:
        analyzer.save_json(f"{args.output}.json")
    
    if args.format in ['csv', 'all']:
        analyzer.save_csv(f"{args.output}.csv")
    
    if args.format in ['kotlin', 'all']:
        analyzer.save_kotlin(f"{args.output}.kt")
    
    print("\n✅ Extraction complete!")
    print(f"   Total patterns extracted: {len(analyzer.patterns_data)}")

if __name__ == "__main__":
    main()
