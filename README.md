# Draw-draw-draw 
**Autor:** Lukáš Levinský  
**Jazyk:** Java 17+ \
**Typ projektu:** Desktopová aplikace pro rastrovou grafiku

---

## 1. Úvod

Draw-draw-draw je jednoduchý, ale zábavný nástroj pro rychlý náčrt a úpravu rastrové grafiky.

## 2. Hlavní funkce

### 2.1 Kreslící nástroje
- **Základní tvary:**
    - Čára
    - Obdélník
    - Trojúhelník
    - Kružnice
    - Vlastní polygon
- **Speciální nástroje:**
    - Štětec s nastavitelnou šířkou
    - Guma
    - Výplň (flood fill)
    - Výběr a transformace objektů

### 2.2 Stylování čar
- **Dynamická změna stylu:**
    - `V` → Tečkovaná čára (2px zapnuto, 2px vypnuto)
    - `B` → Čárkovaná čára (8px zapnuto, 4px vypnuto)
    - (Uvolnění klávesy → Plná čára)
- **Podpora pro všechny tvary** včetně polygonů s plynulým přechodem stylu v rozích

### 2.3 Pokročilé vlastnosti
- Režim přesného kreslení (podržení Shift pro vodorovné/svislé čáry)
- Undo/Redo operace
- Dynamický náhled během kreslení
- Dvojité vyrovnávací paměťování pro plynulé vykreslování

## 3. Architektura

### 3.1 Hlavní komponenty
| Třída | Účel |
|-------|------|
| `App` | Hlavní řídicí třída aplikace |
| `RasterBufferedImage` | Správa bitmapového vykreslování |
| `DoubleBufferedRaster` | Vyrovnávací paměť pro plynulé kreslení |
| `LineRasterizerTrivial` | Algoritmy pro vykreslování čar |
| `PolygonRasterizer` | Vykreslování polygonálních tvarů |
| `FloodFiller` | Implementace výplňového algoritmu |
| `SelectionTracker` | Správa výběru a transformací objektů |

### 3.2 Klíčové algoritmy
- **Bresenhamův algoritmus** (modifikovaný) pro kreslení čar
- **Ray casting** pro detekci výběru tvarů
- **Flood fill** s frontou pro výplň oblastí
- **Pattern continuity** pro plynulé styly čar u polygonů

## 4. Uživatelská příručka

### 4.1 Ovládání
| Akce | Klávesa/Nástroj |
|------|-----------------|
| Kreslení čar | Nástroj Čára |
| Kreslení tvarů | Nástroje Obdélník/Trojúhelník/Kruh |
| Vlastní polygon | Nástroj Polygon + klikání body |
| Výplň oblasti | Nástroj Výplň + klik |
| Změna stylu čar | Podrž `V`/`B` během kreslení |
| Přesné kreslení | Podrž `Shift` |
| Zrušení akce | `Ctrl+Z` nebo tlačítko Undo |