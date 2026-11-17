/** Datos ya traducidos y formateados para el PDF de "Mis productos". */
export interface DatosPdf {
  titulo: string;
  fecha: string;
  columnas: [string, string, string];
  filas: string[][];
  pie: string;
  archivo: string;
}

let librerias: Promise<[typeof import('jspdf'), typeof import('jspdf-autotable')]> | undefined;

function cargarLibrerias() {
  librerias ??= Promise.all([import('jspdf'), import('jspdf-autotable')]);
  return librerias;
}

/**
 * Carga jsPDF por adelantado (cuando hay productos en la lista). Así, al tocar "Descargar PDF" la
 * descarga sale casi en el mismo toque, algo que Safari de iPhone necesita para no bloquearla.
 */
export function precargarPdf(): void {
  cargarLibrerias().catch(() => { librerias = undefined; });
}

/**
 * Arma el PDF en el navegador con jsPDF (se carga recién al pedirlo, así no pesa en la app) y lo
 * descarga. Las fuentes estándar de PDF cubren los acentos de español y portugués; los espacios
 * especiales que usa Intl en los montos se cambian por espacios comunes.
 */
export async function generarPdfProductos(d: DatosPdf): Promise<void> {
  const [{ jsPDF }, { autoTable }] = await cargarLibrerias();
  const limpio = (s: string) => s.replace(/[\u00A0\u202F]/g, ' ');
  const doc = new jsPDF({ unit: 'mm', format: 'a4' });
  const margen = 15;

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(18);
  doc.setTextColor(15, 118, 110);
  doc.text(limpio(d.titulo), margen, 20);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(107, 114, 128);
  doc.text(limpio(d.fecha), margen, 27);

  autoTable(doc, {
    startY: 33,
    margin: { left: margen, right: margen },
    head: [d.columnas.map(limpio)],
    body: d.filas.map((f) => f.map(limpio)),
    styles: { font: 'helvetica', fontSize: 10, cellPadding: 2.5, textColor: [31, 41, 55] },
    headStyles: { fillColor: [15, 118, 110], textColor: 255, fontStyle: 'bold' },
    alternateRowStyles: { fillColor: [246, 247, 249] },
    columnStyles: { 2: { halign: 'right', fontStyle: 'bold', cellWidth: 40 } },
    didParseCell: (c) => { if (c.section === 'head' && c.column.index === 2) c.cell.styles.halign = 'right'; },
    didDrawPage: () => {
      const alto = doc.internal.pageSize.getHeight();
      doc.setFontSize(8);
      doc.setTextColor(150);
      doc.text(limpio(d.pie), margen, alto - 8);
    },
  });

  doc.save(d.archivo);
}
