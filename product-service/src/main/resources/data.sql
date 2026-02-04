-- Seed products for development/testing
-- Electronics
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Sony WH-1000XM5', 'Over-ear Headphones, Silver', 328.00, 85, 'Electronics'),
    ('Nintendo Switch OLED', '7-inch OLED screen', 350.00, 55, 'Electronics'),
    ('Dell UltraSharp 27', '4K USB-C Hub Monitor', 490.00, 220, 'Electronics'),
    ('Google Pixel 8 Pro', '24-hour battery, Porcelain', 200.00, 18, 'Electronics'),
    ('Apple MacBook Pro 14', 'M3 Pro chip, 18GB Unified Memory, 512GB SSD', 1999.00, 12, 'Electronics'),
    ('iPad Pro 11-inch', 'M2 chip, Liquid Retina display, Wi-Fi 6E', 799.00, 30, 'Electronics'),
    ('GoPro HERO12 Black', 'Waterproof Action Camera with HDR Video', 399.99, 145, 'Electronics'),
    ('Logitech MX Master 3S', 'Performance Wireless Mouse, Pale Grey', 99.00, 60, 'Electronics'),
    ('Samsung 990 PRO SSD', '2TB PCIe 4.0 NVMe M.2 Internal SSD', 169.99, 100, 'Electronics')
ON CONFLICT DO NOTHING;

-- Furniture
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Eames Lounge Chair', 'Replica Walnut Plywood with Black Leather', 482.00, 15, 'Furniture'),
    ('Billy Bookcase', 'Oak Effect, adjustable shelves', 80.00, 120, 'Furniture'),
    ('Malm Bed Frame', 'High Bed with 4 storage boxes, White', 200.00, 150, 'Furniture'),
    ('Lack Side Table', 'Black-brown, 55x55 cm', 40.00, 120, 'Furniture')
ON CONFLICT DO NOTHING;

-- Decor
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Fado Table Lamp', 'White Globe Glass lamp with soft light', 56.00, 99, 'Decor'),
    ('Persian Rug Runner', 'Hand-woven wool, 2x8 ft, Traditional pattern', 44.00, 25, 'Decor'),
    ('Large Soy Candle', 'Lavender & Sandalwood, 60-hour burn time', 20.00, 60, 'Decor'),
    ('Canvas Wall Art', 'Abstract Blue Tones, 24x36 inch frame', 30.00, 250, 'Decor')
ON CONFLICT DO NOTHING;

-- Kitchen
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('KitchenAid Artisan Mixer', '5-Quart Tilt-Head Stand Mixer, Empire Red', 449.99, 120, 'Kitchen'),
    ('Nespresso Vertuo Next', 'Coffee and Espresso Machine by De''Longhi', 179.00, 300, 'Kitchen'),
    ('Le Creuset Dutch Oven', '6.75-Quart Enameled Cast Iron, Marseille', 420.00, 80, 'Kitchen'),
    ('Instant Pot Duo Plus', '9-in-1 Electric Pressure Cooker, 6 Quart', 129.95, 50, 'Kitchen'),
    ('Zwilling Knife Block Set', '7-Piece Self-Sharpening Block Set, Silver', 299.00, 15, 'Kitchen')
ON CONFLICT DO NOTHING;

-- Fitness
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Peloton Bike+', 'Stationary Indoor Cycling Bike with 24in Screen', 2495.00, 55, 'Fitness'),
    ('Bowflex SelectTech 552', 'Adjustable Dumbbells (Pair), 5 to 52.5 lbs', 429.00, 90, 'Fitness'),
    ('Theragun Elite', 'Percussive Therapy Deep Tissue Muscle Treatment', 399.00, 88, 'Fitness'),
    ('Manduka PRO Yoga Mat', '6mm Thick Premium Mat, Black Sage', 129.00, 40, 'Fitness'),
    ('Garmin Fenix 7X', 'Multisport GPS Watch, Solar Charging', 899.99, 45, 'Fitness'),
    ('Iron Bull Strength Barbell', 'Olympic Barbell for Powerlifting, Chrome', 285.00, 123, 'Fitness'),
    ('Hypervolt 2 Pro', 'Handheld Percussion Massage Gun, Black', 329.00, 22, 'Fitness'),
    ('NordicTrack Treadmill', 'Commercial 1750, 14-inch Tilt Touchscreen', 1899.00, 6, 'Fitness'),
    ('Concept2 RowErg', 'Model D Indoor Rowing Machine with PM5', 990.00, 88, 'Fitness'),
    ('PowerBlock Pro EXP', 'Stage 1 Adjustable Dumbbell Set, 5-50 lbs', 549.00, 14, 'Fitness')
ON CONFLICT DO NOTHING;

-- Books
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Kindle Paperwhite', '6.8in display, Adjustable Warm Light, 16GB', 149.99, 444, 'Books'),
    ('Hardcover Box Set', 'Lord of the Rings Special Edition, 3 Books', 75.00, 107, 'Books'),
    ('Collector Art Book', 'The Art of Star Wars: The High Republic', 55.00, 556, 'Books'),
    ('Atomic Habits', 'Hardcover, James Clear - Self-Improvement', 16.20, 120, 'Books'),
    ('The Great Gatsby', 'F. Scott Fitzgerald, Classic Literature', 12.00, 50, 'Books'),
    ('Dune Deluxe Edition', 'Frank Herbert, Hardcover with Poster', 35.00, 40, 'Books'),
    ('Project Hail Mary', 'Andy Weir, Science Fiction Thriller', 18.99, 75, 'Books'),
    ('Thinking, Fast and Slow', 'Daniel Kahneman, Behavioral Economics', 15.50, 35, 'Books')
ON CONFLICT DO NOTHING;

-- Media
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Vinyl Record Player', 'Audio-Technica AT-LP60X Fully Automatic', 199.00, 22, 'Media'),
    ('Noise Cancelling Earbuds', 'Bose QuietComfort Ultra, Triple Black', 249.00, 205, 'Media')
ON CONFLICT DO NOTHING;

-- Personal Care
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Dyson Airwrap Multi-Styler', 'Complete Long, Nickel/Copper Finish', 599.99, 77, 'Personal Care'),
    ('Oral-B iO Series 9', 'Electric Toothbrush with 7 Smart Modes', 299.99, 45, 'Personal Care'),
    ('Skincare Starter Set', 'Drunk Elephant Littles Night Out Kit', 85.00, 48, 'Personal Care'),
    ('Waterpik Aquarius', 'Professional Water Flosser, Blue', 99.00, 53, 'Personal Care'),
    ('Beard Trimmer Kit', 'Philips Norelco Series 7000, 23 Pieces', 65.00, 38, 'Personal Care')
ON CONFLICT DO NOTHING;

-- Beauty
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
    ('Revitalizing Face Serum', 'Vitamin C + Hyaluronic Acid, 30ml', 48.00, 75, 'Beauty'),
    ('Matte Velvet Lipstick', 'Long-wear, shade: Classic Crimson', 24.50, 110, 'Beauty'),
    ('Estée Lauder Night Repair', 'Synchronized Multi-Recovery Complex, 50ml', 115.00, 40, 'Beauty'),
    ('Dior Sauvage Eau de Parfum', 'Men''s Fragrance, Bergamot and Vanilla, 100ml', 145.00, 25, 'Beauty'),
    ('Laneige Lip Sleeping Mask', 'Berry Flavor, leave-on lip mask', 24.00, 150, 'Beauty'),
    ('The Ordinary Peeling Solution', 'AHA 30% + BHA 2% Exfoliating Facial', 9.50, 200, 'Beauty'),
    ('NARS Radiant Creamy Concealer', 'Award-winning concealer, shade: Custard', 32.00, 85, 'Beauty')
ON CONFLICT DO NOTHING;
