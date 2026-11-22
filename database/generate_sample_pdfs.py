"""
Generates sample official municipal PDF guidelines for CivicAI RAG testing.
Creates minimal valid PDF 1.4 binary documents without external heavy dependencies.
"""
import os
from pathlib import Path

def create_simple_pdf(filepath: str, title: str, sections: list):
    """Generates a valid standalone PDF 1.4 file containing the specified text sections."""
    lines = [f"{title}", "==================================================", ""]
    for header, content in sections:
        lines.append(header)
        lines.append("-" * len(header))
        lines.append(content)
        lines.append("")

    stream_content = "BT\n/F1 11 Tf\n50 750 Td\n14 TL\n"
    for line in lines:
        # Escape parenthesis
        clean_line = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
        stream_content += f"({clean_line}) '\n"
    stream_content += "ET\n"

    stream_len = len(stream_content.encode('latin1'))

    pdf_template = f"""%PDF-1.4
1 0 obj
<< /Type /Catalog /Pages 2 0 R >>
endobj
2 0 obj
<< /Type /Pages /Kids [3 0 R] /Count 1 >>
endobj
3 0 obj
<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>
endobj
4 0 obj
<< /Length {stream_len} >>
stream
{stream_content}endstream
endobj
5 0 obj
<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>
endobj
xref
0 6
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000244 00000 n 
0000000{305 + stream_len:03d} 00000 n 
trailer
<< /Size 6 /Root 1 0 R >>
startxref
{370 + stream_len}
%%EOF"""

    Path(filepath).parent.mkdir(parents=True, exist_ok=True)
    with open(filepath, "wb") as f:
        f.write(pdf_template.encode('latin1'))
    print(f"[+] Created PDF: {filepath}")

def main():
    docs_dir = Path(__file__).resolve().parent.parent / "uploads" / "documents"

    # 1. Road Repair SOP
    create_simple_pdf(
        str(docs_dir / "SOP_Road_Pothole_Repair_v2.pdf"),
        "MUNICIPAL STANDARD OPERATING PROCEDURE: ROAD & POTHOLE REPAIR (SOP-RD-04)",
        [
            ("SECTION 1: PURPOSE AND JURISDICTION",
             "This protocol dictates safety parameters and material specifications for public road cavities, asphalt cracks, and street settlement across municipal zones."),
            ("SECTION 2: SAFETY PERIMETER AND TRAFFIC CONTROLS",
             "Emergency crews must deploy retroreflective cones 15 meters in advance of any open excavation or crater exceeding 300mm in diameter. Lane closures must follow uniform traffic code."),
            ("SECTION 3: TECHNICAL REPAIR SPECIFICATIONS",
             "Excavate all damaged edge surfaces down to sound pavement. Remove moisture and loose stones using pneumatic air jet. Apply asphalt tack coat SS-1h at 0.3 liters per square meter. Compact hot-mix Type B asphalt in lifts not exceeding 50mm to 95 percent laboratory density."),
            ("SECTION 4: VERIFICATION AND CLOSURE",
             "Field inspector must measure surface evenness with a 3-meter straightedge. Any deviation exceeding 6mm requires corrective milling before opening to vehicular traffic.")
        ]
    )

    # 2. Electrical Lighting SOP
    create_simple_pdf(
        str(docs_dir / "SOP_Electrical_Lighting_Safety.pdf"),
        "PUBLIC LIGHTING & ELECTRICAL HAZARD INTERVENTION (SOP-EL-09)",
        [
            ("SECTION 1: HIGH VOLTAGE PERIMETER PROTOCOL",
             "All downed lighting poles, fractured conduit boxes, or exposed cabling must be treated as energized 240V or 480V lines. First responders must secure an 8-meter exclusion perimeter immediately."),
            ("SECTION 2: GRID ISOLATION AND GROUNDING",
             "Notify municipal electrical dispatch to trip circuit breakers on feeder line FL-2. Ground both ends of the service loop before touching any metal luminaire structure."),
            ("SECTION 3: EQUIPMENT REPLACEMENT & TESTING",
             "Replace fractured polycarbonate protective covers. Measure grounding resistance ensuring it reads below 5 ohms before restoring circuit continuity.")
        ]
    )

    # 3. Water Infrastructure SOP
    create_simple_pdf(
        str(docs_dir / "SOP_Water_Infrastructure_Rupture.pdf"),
        "MUNICIPAL WATER MAIN RUPTURE & LEAKAGE RESPONSE (SOP-WT-01)",
        [
            ("SECTION 1: EMERGENCIES AND ROAD FOUNDATION PRESERVATION",
             "Water escaping under pressure rapidly erodes road sub-base material, creating sudden underground sinkholes. Dispatch crews must isolate the section within 60 minutes."),
            ("SECTION 2: GATE VALVE SHUTOFF AND DEPRESSURIZATION",
             "Locate nearest upstream and downstream mechanical gate valves. Slowly turn clockwise to stop main flow and avoid water hammer shockwave."),
            ("SECTION 3: REPAIR SLEEVES AND RE-PAVING",
             "For circumferential fractures, apply 316 stainless-steel full-circle repair clamp. Backfill excavation with compacted crushed gravel and notify Roads Department for asphalt resurfacing.")
        ]
    )

if __name__ == "__main__":
    main()
