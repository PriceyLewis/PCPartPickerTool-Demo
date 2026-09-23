# Demo catalogue and example baskets

The shared `PCPartPicker/src/Assets/catalogue.tsv` contains 130 named components across eight categories. Product names identify demonstration entries; prices, inventory and seeded reviews are fictional, not current retailer information. Specifications and physical compatibility have not been validated. This app compares listings and builds baskets; it does not implement a compatibility engine.

The browser seeds 390 store listings from this file. The bundled Access database contains the same 130 components and 390 matching listings, plus 17 retained coursework listings at other stores (407 total). Original part IDs, users, history, stores and relationships are preserved. Six older names are normalised to avoid duplicate products.

## Coverage

| Category | Components |
|---|---:|
| CPU | 23 |
| GPU | 26 |
| RAM | 15 |
| Motherboard | 19 |
| Storage | 17 |
| Case | 10 |
| PSU | 10 |
| Cooling | 10 |

## Example shopping baskets

These are reproducible sample shopping lists to demonstrate the basket and budget totals, not validated purchasing recommendations. Search each name and select the store shown below to reproduce these initial fictional totals. No preset-loading feature is implied.

### Budget — sample total £779.92

- Ryzen 5 5600 — Scan Computers: £119.99
- GeForce RTX 3050 — Scan Computers: £259.99
- B550-A PRO — Overclockers UK: £109.99
- Vengeance LPX 16GB DDR4 — Scan Computers: £34.99
- Blue SN580 1TB NVMe SSD — Overclockers UK: £59.99
- 3000D Airflow Case — Scan Computers: £109.99
- CX550 PSU — Scan Computers: £49.99
- Pure Rock 2 Cooler — Scan Computers: £34.99

### Mid-range — sample total £1,464.92

- Ryzen 5 7600 — Scan Computers: £244.99
- GeForce RTX 4070 Super — Scan Computers: £579.99
- MAG B650 Tomahawk WiFi — Scan Computers: £179.99
- Vengeance 32GB DDR5 — Scan Computers: £109.99
- 980 Pro 1TB NVMe SSD — Scan Computers: £119.99
- 4000D Airflow Case — Scan Computers: £69.99
- RM750x PSU — Scan Computers: £109.99
- Freezer 36 Cooler — Scan Computers: £49.99

### High-end — sample total £2,110.92

- Ryzen 7 7800X3D — Overclockers UK: £334.99
- GeForce RTX 4090 — Scan Computers: £974.99
- TUF Gaming B650-PLUS — Scan Computers: £169.99
- Fury Beast 64GB DDR5 — Scan Computers: £105.99
- 990 Pro 2TB NVMe SSD — Scan Computers: £144.99
- H9 Flow Case — Scan Computers: £139.99
- RM1000x PSU — Scan Computers: £149.99
- Liquid Freezer III 360 Cooler — Scan Computers: £89.99

## Demonstration edge cases

- `Core i9-13900K` is sold out at all three shared stores: it appears in admin/low-stock data but is excluded from in-stock search and recommendations.
- Some other store listings have zero stock, while alternatives remain available.
- Low-stock rows exercise the admin warning view.
- Ask for `GPU under £5` to exercise the no-match response.
- Ask for `RTX 4090 GPU under £1500` to verify expensive products are not excluded by an arbitrary cheapest-30 limit.
- Combining DDR4 and DDR5 entries or unrelated motherboard/CPU entries demonstrates a limitation: the basket currently accepts them and does not validate compatibility.

## Maintaining the fixtures

Edit the TSV once, then rebuild the packaged resource with `mvn compile`. Compile `tools/SeedCatalogue.java` against `target/classes` and the Maven dependency classpath, and run `SeedCatalogue /path/to/demo-copy.accdb`. The explicit maintenance tool upserts catalogue listings and labels existing seeded reviews. It is never invoked by the application and must be used on a copy of the demo fixture, not a personal working database. Replace the bundled fixture after reviewing the result, then run `mvn verify`.

The catalogue tests compare every shared Access listing with the seed, verify all categories and unique names, and exercise recommendation coverage and stock filtering. Browser data remains disposable on reload. Desktop edits remain persistent.
