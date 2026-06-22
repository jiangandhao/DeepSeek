from app.reports.analyzer import _parse_indicators


def test_parse_common_lab_indicators():
    text = "空腹血糖 7.2 mmol/L\n糖化血红蛋白 6.8 %\n尿酸 460 umol/L\nALT 28 U/L"
    items = _parse_indicators(text)
    by_code = {item["code"]: item for item in items}
    assert by_code["GLU"]["status"] == "HIGH"
    assert by_code["HBA1C"]["status"] == "HIGH"
    assert by_code["UA"]["status"] == "HIGH"
    assert by_code["ALT"]["status"] == "NORMAL"


def test_report_range_is_structured():
    items = _parse_indicators("总胆固醇: 4.5 mmol/L")
    assert items[0]["reference"] == "0–5.2"
    assert items[0]["unit"] == "mmol/L"
