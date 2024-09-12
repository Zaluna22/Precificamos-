
# Calculadora de Custos de Produtos

## Descrição

Este é um projeto pessoal desenvolvido para ajudar empreendedores a calcular o custo de seus produtos com base em materiais. O sistema permite adicionar materiais, calcular o custo total, guardar produtos e exportar relatórios em PDF.

## Funcionalidades

1. **Adicionar Material**: Permite adicionar múltiplos materiais com nome, preço por kg e quantidade em gramas.
2. **Calcular Custo Total**: Calcula o custo final do produto com base nos materiais inseridos, adicionando 40% ao custo total e multiplicando o resultado por 3.
3. **Remover Material**: Permite remover materiais individuais.
4. **Limpar Materiais**: Limpa todos os materiais e o resultado da calculadora.
5. **Guardar Materiais**: Armazena produtos e materiais no `localStorage` do navegador.
6. **Exibir Produtos**: Mostra a lista de produtos salvos com seus custos e detalhes dos materiais.
7. **Eliminar Produto**: Remove produtos salvos do `localStorage`.
8. **Gerar PDF**: Exporta a lista de produtos e seus detalhes em um arquivo PDF.

## Instruções de Uso

1. **Adicionar Material**:
   - Clique no botão "Adicionar Material" para criar um novo campo de material.
   - Preencha os campos de nome, preço por kg e quantidade em gramas.
   - Clique em "Remover" para excluir um material específico.

2. **Calcular Custo Total**:
   - Insira o nome do produto e preencha os campos de material.
   - Clique no botão "Calcular" para visualizar o custo final do produto.

3. **Guardar Materiais**:
   - Após preencher todos os materiais e o nome do produto, clique em "Guardar Materiais".
   - Os dados serão salvos no `localStorage` e exibidos na lista de produtos.

4. **Exibir Produtos**:
   - A lista de produtos salvos é atualizada automaticamente e pode ser visualizada na tela principal.

5. **Eliminar Produto**:
   - Na lista de produtos, clique no botão "Eliminar Produto" para remover um produto específico.

6. **Gerar PDF**:
   - Clique no botão "Gerar PDF" para baixar um relatório com todos os produtos salvos em formato PDF.

## Código

O projeto utiliza JavaScript para manipulação DOM e `localStorage` para armazenamento. O código principal é dividido nas seguintes funções:

- **adicionarMaterial()**: Adiciona um novo campo de material ao formulário.
- **calcular()**: Calcula o custo total do produto com base nos materiais.
- **removerMaterial(button)**: Remove um material específico.
- **limparMateriais()**: Limpa todos os materiais e o resultado da calculadora.
- **guardarMateriais()**: Salva os dados do produto no `localStorage`.
- **exibirProdutos()**: Exibe os produtos salvos e seus detalhes.
- **eliminarProduto(index)**: Remove um produto salvo do `localStorage`.
- **gerarPDF()**: Gera um relatório em PDF com os produtos salvos.

## Dependências

- [jsPDF](https://github.com/eKoopmans/html2pdf) para geração de PDF.

## Instalação

Inclua o script `jsPDF` em seu HTML para suporte à geração de PDF:

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
```

## Contribuição

Este é um projeto pessoal e não aceita contribuições externas no momento.

## Licença

Este projeto é de domínio público e pode ser utilizado, modificado e distribuído livremente.

