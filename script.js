// Função para adicionar material
function adicionarMaterial() {
    const materiaisDiv = document.getElementById('materiais');
  
    const novoMaterial = document.createElement('div');
    novoMaterial.classList.add('material');
    novoMaterial.innerHTML = `
      <label>Material:</label>
      <input type="text" class="material-nome">
      <label>Preço por Kg:</label>
      <input type="number" class="material-preco">
      <label>Quantidade (gramas):</label>
      <input type="number" class="material-quantidade">
      <button class="remover" onclick="removerMaterial(this)">Remover</button>
    `;
    materiaisDiv.appendChild(novoMaterial);
  }
  
  // Função para calcular o custo total
  function calcular() {
      const produto = document.getElementById('produto').value;
      let materiais = [];
    
      const nomesMateriais = document.querySelectorAll('.material-nome');
      const precosPorKg = document.querySelectorAll('.material-preco');
      const quantidadesEmGramas = document.querySelectorAll('.material-quantidade');
    
      let erro = false;
      nomesMateriais.forEach((nome, index) => {
        const preco = parseFloat(precosPorKg[index].value);
        const quantidade = parseFloat(quantidadesEmGramas[index].value);
    
        if (!nome.value || isNaN(preco) || isNaN(quantidade)) {
          alert('Por favor, preencha todos os campos de material corretamente.');
          erro = true;
          return;
        }
    
        materiais.push({
          nome: nome.value,
          precoPorKg: preco,
          quantidadeEmGramas: quantidade
        });
      });
    
      if (erro) return;
    
      let custoTotal = 0;
      materiais.forEach(material => {
        const quantidadeEmKg = material.quantidadeEmGramas / 1000;
        custoTotal += material.precoPorKg * quantidadeEmKg;
      });
    
      // Calcula o custo final com 40% a mais e multiplicado por 3
      
      
      const custoFinal = (custoTotal * 1.40) * 3;

    
      const resultado = document.getElementById('resultado');
      resultado.textContent = `O custo final do produto "${produto}" é R$ ${custoFinal.toFixed(2)}.`;
    }
    
  // Função para remover um material individual
  function removerMaterial(button) {
    const materialDiv = button.parentNode;
    materialDiv.remove();
  }
  
  // Função para eliminar todos os materiais
  function limparMateriais() {
    const materiaisDiv = document.getElementById('materiais');
    materiaisDiv.innerHTML = ''; // Remove todos os elementos visuais de materiais
    document.getElementById('resultado').textContent = ''; // Limpa o resultado
    document.getElementById('produto').value = ''; // Limpa o nome do produto
  }
  
  // Função para guardar materiais no localStorage
  function guardarMateriais() {
    const produto = document.getElementById('produto').value;
    const nomesMateriais = document.querySelectorAll('.material-nome');
    const precosPorKg = document.querySelectorAll('.material-preco');
    const quantidadesEmGramas = document.querySelectorAll('.material-quantidade');
  
    let materiais = [];
    nomesMateriais.forEach((nome, index) => {
      const preco = parseFloat(precosPorKg[index].value);
      const quantidade = parseFloat(quantidadesEmGramas[index].value);
  
      if (nome.value && !isNaN(preco) && !isNaN(quantidade)) {
        materiais.push({
          nome: nome.value,
          precoPorKg: preco,
          quantidadeEmGramas: quantidade
        });
      }
    });
  
    const dadosProduto = {
      produto: produto,
      materiais: materiais
    };
  
    // Recupera produtos existentes
    let produtosGuardados = JSON.parse(localStorage.getItem('dadosProdutos')) || [];
    
    // Adiciona o novo produto à lista
    produtosGuardados.push(dadosProduto);
  
    localStorage.setItem('dadosProdutos', JSON.stringify(produtosGuardados)); // Guarda a lista de produtos no localStorage
    alert('Materiais guardados com sucesso!');
  
    // Limpa a calculadora para permitir novo produto
    limparMateriais();
    exibirProdutos(); // Atualiza a exibição
  }
  
 // Função para exibir produtos salvos
function exibirProdutos() {
    const listaProdutos = document.getElementById('lista-produtos');
    listaProdutos.innerHTML = ''; // Limpa a lista antes de adicionar novos itens
  
    const dadosGuardados = JSON.parse(localStorage.getItem('dadosProdutos'));
    if (dadosGuardados) {
      dadosGuardados.forEach((produto, index) => {
        // Calcula o custo total dos materiais
        let custoTotalMateriais = produto.materiais.reduce((total, material) => 
          total + (material.precoPorKg * material.quantidadeEmGramas / 1000), 0
        );
  
        // Adiciona 40% ao custo total e multiplica o resultado por 3
        const custoAdicional = (custoTotalMateriais * 1.4) 
        const custoFinal = (custoAdicional * 3)
        
  
        const item = document.createElement('li');
        item.innerHTML = `
          <strong>Produto:</strong> ${produto.produto} <br>
          <strong>Custo Total:</strong> R$ ${custoFinal.toFixed(2)} <br>
          <strong>Materiais:</strong> <br>
          <ul>
            ${produto.materiais.map(material => 
              `<li>${material.nome}: ${material.quantidadeEmGramas}g - R$ ${material.precoPorKg.toFixed(2)}/kg</li>`
            ).join('')}
          </ul>
          <button onclick="eliminarProduto(${index})">Eliminar Produto</button>
        `;
        listaProdutos.appendChild(item);
      });
    } else {
      listaProdutos.innerHTML = '<p>Nenhum produto salvo ainda.</p>';
    }
  }
  
    
    // Função para eliminar produto
    function eliminarProduto(index) {
      let dadosGuardados = JSON.parse(localStorage.getItem('dadosProdutos'));
      if (dadosGuardados) {
        dadosGuardados.splice(index, 1); // Remove o produto da lista
        localStorage.setItem('dadosProdutos', JSON.stringify(dadosGuardados));
        exibirProdutos(); // Atualiza a lista de produtos
      }
    }
    
  
  
  
  // Carregar e exibir produtos ao carregar a página
  window.onload = function() {
    exibirProdutos();
  };
  


  // Função para gerar PDF
async function gerarPDF() {
  const { jsPDF } = window.jspdf;

  const dadosGuardados = JSON.parse(localStorage.getItem('dadosProdutos'));
  if (!dadosGuardados) {
    alert('Nenhum produto para exportar.');
    return;
  }

  const doc = new jsPDF();
  doc.setFontSize(12);
  doc.text('Lista de Produtos', 10, 10);

  let y = 20;

  dadosGuardados.forEach((produto, index) => {
    let custoTotalMateriais = produto.materiais.reduce((total, material) => 
        total + (material.precoPorKg * material.quantidadeEmGramas / 1000), 0
      );
      const custoFinal = (custoTotalMateriais * 1.4) * 3;
      
    doc.setFontSize(12);
    doc.text(`Produto ${index + 1}: ${produto.produto}`, 10, y);
    y += 10;

    // Mostra o custo total com os acréscimos corretos
    doc.text(`Custo Total: R$ ${custoFinal.toFixed(2)}`, 10, y);
    y += 10;

    doc.text('Materiais:', 10, y);
    y += 10;

    produto.materiais.forEach(material => {
      doc.text(`- ${material.nome}: ${material.quantidadeEmGramas}g - R$ ${material.precoPorKg.toFixed(2)}/kg`, 10, y);
      y += 10;
    });

    y += 10; // Espaço entre produtos
  });

  doc.save('produtos.pdf');
}

  