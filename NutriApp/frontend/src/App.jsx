import React, { useState, useEffect } from 'react';
import { Calculator, Package, BookOpen, Home, Plus, Search, Trash2, QrCode, Download } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';

const formatValue = (value, decimals = 1) => {
  if (value === null || value === undefined) return '0,0'
  return Number(value).toFixed(decimals).replace('.', ',')
};

const Dashboard = ({ ingredientsCount, recipesCount }) => {
  const stats = [
    { icon: Package, label: 'Ingredientes TBCA', value: ingredientsCount, color: 'bg-blue-500' },
    { icon: BookOpen, label: 'Receitas', value: recipesCount, color: 'bg-green-500' },
    { icon: Calculator, label: 'Conformidade', value: '100%', color: 'bg-purple-500' }
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-gray-900">Dashboard</h2>
        <p className="mt-2 text-gray-600">Bem-vindo ao NutriApp</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat, idx) => (
          <div key={idx} className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className={`p-3 rounded-full ${stat.color}`}>
                <stat.icon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">{stat.label}</p>
                <p className="text-2xl font-semibold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold mb-4">🎯 Funcionalidades</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {['Cálculos automáticos ANVISA', 'Base TBCA oficial', 'Geração de QR Code', 'Exportação múltipla'].map((f, i) => (
            <div key={i} className="flex items-start">
              <span className="text-green-500 mr-2">✓</span>
              <span className="text-gray-700">{f}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const IngredientsPage = ({ ingredients, onReload }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filtered, setFiltered] = useState(ingredients);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({
    name: '', portionUnit: 'g', energyKcal: '', carbohydrates: '', proteins: '', totalFats: '',
    dietaryFiber: '', sodium: '', tbcaCode: '', category: ''
  });

  useEffect(() => { setFiltered(ingredients); }, [ingredients]);

  const handleSearch = async () => {
    if (searchTerm.trim()) {
      try {
        const res = await fetch(`${API_BASE_URL}/ingredients/search?q=${encodeURIComponent(searchTerm)}`);
        setFiltered(await res.json());
      } catch (e) { alert('Erro na pesquisa'); }
    } else {
      setFiltered(ingredients);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Excluir ingrediente?')) {
      try {
        const res = await fetch(`${API_BASE_URL}/ingredients/${id}`, { method: 'DELETE' });
        if (res.ok) {
          
          onReload();
          return;
        }
        if (res.status === 409) {
          const txt = await res.text();
          alert(txt || 'Ingrediente não pode ser excluído porque está associado a uma ou mais receitas.');
          return;
        }
        const txt = await res.text();
        alert('Erro ao excluir: ' + (txt || res.statusText));
      } catch (e) { alert('Erro ao excluir'); }
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!form.name || !form.portionUnit) { alert('Nome e unidade são obrigatórios'); return; }
   
    const payload = {
      name: form.name,
      portionUnit: form.portionUnit,
      energyKcal: form.energyKcal ? parseFloat(form.energyKcal) : null,
      carbohydrates: form.carbohydrates ? parseFloat(form.carbohydrates) : null,
      proteins: form.proteins ? parseFloat(form.proteins) : null,
      totalFats: form.totalFats ? parseFloat(form.totalFats) : null,
      dietaryFiber: form.dietaryFiber ? parseFloat(form.dietaryFiber) : null,
      sodium: form.sodium ? parseFloat(form.sodium) : null,
      tbcaCode: form.tbcaCode || null,
      category: form.category || null
    };

    try {
      const res = await fetch(`${API_BASE_URL}/ingredients`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      if (res.status === 201) {
        setForm({ name: '', portionUnit: 'g', energyKcal: '', carbohydrates: '', proteins: '', totalFats: '', dietaryFiber: '', sodium: '', tbcaCode: '', category: '' });
        setShowCreate(false);
        onReload();
      } else {
        const txt = await res.text();
        alert('Erro ao criar ingrediente: ' + (txt || res.statusText));
      }
    } catch (err) {
      alert('Erro ao criar ingrediente');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold">Ingredientes</h2>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">{filtered.length} ingredientes</span>
          <button onClick={() => setShowCreate(!showCreate)} className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center">
            <Plus className="h-4 w-4 mr-2" />{showCreate ? 'Cancelar' : 'Novo'}
          </button>
        </div>
      </div>

      {showCreate && (
        <form onSubmit={handleCreate} className="bg-white rounded-lg shadow p-6 space-y-4">
          <div className="grid grid-cols-3 gap-4">
            <input type="text" placeholder="Nome" value={form.name} onChange={e => setForm(f => ({...f, name: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <select value={form.portionUnit} onChange={e => setForm(f => ({...f, portionUnit: e.target.value}))} className="px-3 py-2 border rounded-lg">
              <option value="g">Gramas (g)</option>
              <option value="ml">Mililitros (ml)</option>
            </select>
            <input type="text" placeholder="Categoria" value={form.category} onChange={e => setForm(f => ({...f, category: e.target.value}))} className="px-3 py-2 border rounded-lg" />
          </div>
          <div className="grid grid-cols-4 gap-4">
            <input type="number" step="0.01" placeholder="Energia (kcal)" value={form.energyKcal} onChange={e => setForm(f => ({...f, energyKcal: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <input type="number" step="0.01" placeholder="Carboidratos (g)" value={form.carbohydrates} onChange={e => setForm(f => ({...f, carbohydrates: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <input type="number" step="0.01" placeholder="Proteínas (g)" value={form.proteins} onChange={e => setForm(f => ({...f, proteins: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <input type="number" step="0.01" placeholder="Gorduras totais (g)" value={form.totalFats} onChange={e => setForm(f => ({...f, totalFats: e.target.value}))} className="px-3 py-2 border rounded-lg" />
          </div>
          <div className="grid grid-cols-3 gap-4">
            <input type="number" step="0.01" placeholder="Fibra (g)" value={form.dietaryFiber} onChange={e => setForm(f => ({...f, dietaryFiber: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <input type="number" step="0.01" placeholder="Sódio (mg)" value={form.sodium} onChange={e => setForm(f => ({...f, sodium: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <input type="text" placeholder="Código TBCA" value={form.tbcaCode} onChange={e => setForm(f => ({...f, tbcaCode: e.target.value}))} className="px-3 py-2 border rounded-lg" />
          </div>
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => { setShowCreate(false); setForm({ name: '', portionUnit: 'g', energyKcal: '', carbohydrates: '', proteins: '', totalFats: '', dietaryFiber: '', sodium: '', tbcaCode: '', category: '' }); }} className="px-4 py-2 border rounded-lg">Cancelar</button>
            <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg">Criar</button>
          </div>
        </form>
      )}

      <div className="flex gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder="Pesquisar..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="w-full pl-10 pr-4 py-2 border rounded-lg"
          />
        </div>
        <button onClick={handleSearch} className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
          Buscar
        </button>
      </div>

      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nome</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Energia</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Carb</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Prot</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Gord</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Ações</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filtered.map((ing) => (
              <tr key={ing.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{ing.name}</div>
                  <div className="text-xs text-gray-500">por 100{ing.portionUnit}</div>
                </td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.energyKcal)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.carbohydrates)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.proteins)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.totalFats)}</td>
                <td className="px-6 py-4 text-center">
                  <button onClick={() => handleDelete(ing.id)} className="text-red-600 hover:text-red-800">
                    <Trash2 className="h-4 w-4" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="text-sm text-gray-600">Fonte: TBCA - USP</p>
    </div>
  );
};

const RecipesPage = ({ recipes, ingredients, onReload, onCalculate }) => {
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', preparationMethod: 'RAW', totalPortion: '', portionUnit: 'g', servings: '', ingredients: [] });
  const [selIng, setSelIng] = useState('');
  const [qty, setQty] = useState('');

  const methods = { RAW: 'Cru', BOILED: 'Cozido', FRIED: 'Frito', BAKED: 'Assado', GRILLED: 'Grelhado', STEAMED: 'Vapor' };

  const addIngredient = () => {
    if (!selIng || !qty) { alert('Selecione ingrediente e quantidade'); return; }
    const ing = ingredients.find(i => i.id.toString() === selIng);
    if (form.ingredients.some(i => i.ingredient.id === ing.id)) { alert('Já adicionado'); return; }
    setForm(p => ({ ...p, ingredients: [...p.ingredients, { ingredient: ing, quantity: parseFloat(qty) }] }));
    setSelIng(''); setQty('');
  };

  const submitRecipe = async () => {
    if (!form.name || !form.totalPortion || form.ingredients.length === 0) { alert('Preencha todos os campos'); return; }
    try {
      await fetch(`${API_BASE_URL}/recipes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      alert('Receita criada!');
      setShowForm(false);
      setForm({ name: '', preparationMethod: 'RAW', totalPortion: '', portionUnit: 'g', servings: '', ingredients: [] });
      onReload();
    } catch (e) { alert('Erro'); }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold">Receitas</h2>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
          <Plus className="h-4 w-4 mr-2" />Nova
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg shadow p-6 space-y-4">
          <h3 className="text-lg font-semibold">Nova Receita</h3>
          <div className="grid grid-cols-2 gap-4">
            <input type="text" placeholder="Nome" value={form.name} onChange={(e) => setForm(p => ({...p, name: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <select value={form.preparationMethod} onChange={(e) => setForm(p => ({...p, preparationMethod: e.target.value}))} className="px-3 py-2 border rounded-lg">
              {Object.entries(methods).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <input type="number" placeholder="Porção" value={form.totalPortion} onChange={(e) => setForm(p => ({...p, totalPortion: e.target.value}))} className="px-3 py-2 border rounded-lg" />
            <select value={form.portionUnit} onChange={(e) => setForm(p => ({...p, portionUnit: e.target.value}))} className="px-3 py-2 border rounded-lg">
              <option value="g">Gramas</option>
              <option value="ml">ml</option>
            </select>
            <input type="number" placeholder="Porções" value={form.servings} onChange={(e) => setForm(p => ({...p, servings: e.target.value}))} className="px-3 py-2 border rounded-lg" />
          </div>
          <div className="grid grid-cols-3 gap-2">
            <select value={selIng} onChange={(e) => setSelIng(e.target.value)} className="px-3 py-2 border rounded-lg">
              <option value="">Ingrediente</option>
              {ingredients.map(i => <option key={i.id} value={i.id}>{i.name}</option>)}
            </select>
            <input type="number" placeholder="Qtd" value={qty} onChange={(e) => setQty(e.target.value)} className="px-3 py-2 border rounded-lg" />
            <button onClick={addIngredient} className="px-4 py-2 bg-green-600 text-white rounded-lg"><Plus className="h-4 w-4" /></button>
          </div>
          {form.ingredients.length > 0 && (
            <div className="border rounded-lg p-4">
              {form.ingredients.map((item, i) => (
                <div key={i} className="flex justify-between p-2 bg-gray-50 rounded mb-2">
                  <span className="text-sm">{item.ingredient.name} - {item.quantity}{item.ingredient.portionUnit}</span>
                  <button onClick={() => setForm(p => ({ ...p, ingredients: p.ingredients.filter((_, idx) => idx !== i) }))} className="text-red-600">
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>
          )}
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowForm(false)} className="px-4 py-2 border rounded-lg">Cancelar</button>
            <button onClick={submitRecipe} className="px-4 py-2 bg-blue-600 text-white rounded-lg">Criar</button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg shadow divide-y">
        {recipes.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Nenhuma receita</div>
        ) : recipes.map(r => (
          <div key={r.id} className="p-6 flex justify-between items-center">
            <div>
              <h4 className="text-lg font-medium">{r.name}</h4>
              <span className="text-sm text-gray-600">{methods[r.preparationMethod]} • {r.totalPortion}{r.portionUnit}</span>
            </div>
            <div className="flex gap-2">
              <button onClick={() => onCalculate(r.id)} className="p-2 text-green-600 hover:bg-green-50 rounded">
                <Calculator className="h-5 w-5" />
              </button>
              <button onClick={async () => { if(confirm('Excluir?')) { await fetch(`${API_BASE_URL}/recipes/${r.id}`, {method:'DELETE'}); onReload(); }}} className="p-2 text-red-600 hover:bg-red-50 rounded">
                <Trash2 className="h-5 w-5" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const NutritionPage = ({ nutritionData, recipes, onCalculate }) => {
  const [selRecipe, setSelRecipe] = useState('');

  const downloadQR = async () => {
    if (!nutritionData) return;
    try {
      const res = await fetch(`${API_BASE_URL}/nutrition/qrcode/${nutritionData.recipeId}?format=PNG`, {method:'POST'});
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `qr-${nutritionData.recipeId}.png`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) { alert('Erro ao gerar QR'); }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold">Tabela Nutricional</h2>
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex gap-4">
          <select value={selRecipe} onChange={(e) => setSelRecipe(e.target.value)} className="flex-1 px-3 py-2 border rounded-lg">
            <option value="">Selecione...</option>
            {recipes.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
          <button onClick={() => selRecipe && onCalculate(parseInt(selRecipe))} disabled={!selRecipe}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300">
            Calcular
          </button>
        </div>
      </div>

      {nutritionData ? (
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <div className="xl:col-span-2 bg-white rounded-lg shadow overflow-hidden">
            <div className="bg-black text-white text-center p-4">
              <h3 className="text-lg font-bold">INFORMAÇÃO NUTRICIONAL</h3>
              <p className="text-sm">{nutritionData.recipeName}</p>
              <p className="text-sm">Porção de 100g</p>
            </div>
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-black">
                  <th className="text-left p-3 font-bold">Nutriente</th>
                  <th className="text-center p-3 font-bold">Qtd/porção</th>
                  <th className="text-center p-3 font-bold">%VD*</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b"><td className="p-3 font-semibold">Valor energético</td>
                  <td className="text-center p-3">{formatValue(nutritionData.energyKcal)} kcal</td>
                  <td className="text-center p-3">{formatValue(nutritionData.energyDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Carboidratos</td>
                  <td className="text-center p-3">{formatValue(nutritionData.carbohydrates)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.carbohydratesDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Proteínas</td>
                  <td className="text-center p-3">{formatValue(nutritionData.proteins)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.proteinsDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Gorduras totais</td>
                  <td className="text-center p-3">{formatValue(nutritionData.totalFats)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.totalFatsDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Fibra alimentar</td>
                  <td className="text-center p-3">{formatValue(nutritionData.dietaryFiber)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.dietaryFiberDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Sódio</td>
                  <td className="text-center p-3">{formatValue(nutritionData.sodium, 0)} mg</td>
                  <td className="text-center p-3">{formatValue(nutritionData.sodiumDV)}%</td></tr>
              </tbody>
            </table>
            <div className="p-4 text-xs text-gray-600">
              <p>*% Valores Diários. Conforme RDC nº 429/2020 ANVISA</p>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold mb-4 flex items-center">
              <Download className="h-5 w-5 mr-2" />Exportar
            </h3>
            <button onClick={downloadQR} className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center justify-center">
              <QrCode className="h-4 w-4 mr-2" />QR Code PNG
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <Calculator className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <p className="text-gray-500">Selecione uma receita</p>
        </div>
      )}
    </div>
  );
};

const NutriApp = () => {
  const [tab, setTab] = useState('dashboard');
  const [ingredients, setIngredients] = useState([]);
  const [recipes, setRecipes] = useState([]);
  const [nutritionData, setNutritionData] = useState(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [ing, rec] = await Promise.all([
        fetch(`${API_BASE_URL}/ingredients`).then(r => r.json()),
        fetch(`${API_BASE_URL}/recipes`).then(r => r.json())
      ]);
      setIngredients(ing);
      setRecipes(rec);
    } catch (e) { console.error(e); }
  };

  const calculate = async (recipeId) => {
    try {
      const res = await fetch(`${API_BASE_URL}/nutrition/recipe/${recipeId}`);
      setNutritionData(await res.json());
      setTab('nutrition');
    } catch (e) { alert('Erro ao calcular'); }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm border-b sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 h-16 flex justify-between items-center">
          <div className="flex items-center">
            <div className="bg-gradient-to-r from-blue-600 to-green-500 p-2 rounded-lg">
              <Calculator className="h-6 w-6 text-white" />
            </div>
            <div className="ml-3">
              <h1 className="text-xl font-bold">NutriApp</h1>
              <p className="text-xs text-gray-500">Sistema de Índice Nutricional</p>
            </div>
          </div>
          <nav className="flex space-x-2">
            {[
              { id: 'dashboard', icon: Home, label: 'Dashboard' },
              { id: 'ingredients', icon: Package, label: 'Ingredientes' },
              { id: 'recipes', icon: BookOpen, label: 'Receitas' },
              { id: 'nutrition', icon: Calculator, label: 'Tabela' }
            ].map(t => (
              <button key={t.id} onClick={() => setTab(t.id)}
                className={`flex items-center px-4 py-2 text-sm font-medium rounded-lg ${
                  tab === t.id ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'
                }`}>
                <t.icon className="h-4 w-4 mr-2" />{t.label}
              </button>
            ))}
          </nav>
        </div>
      </header>
      <main className="max-w-7xl mx-auto py-6 px-4">
        {tab === 'dashboard' && <Dashboard ingredientsCount={ingredients.length} recipesCount={recipes.length} />}
        {tab === 'ingredients' && <IngredientsPage ingredients={ingredients} onReload={loadData} />}
        {tab === 'recipes' && <RecipesPage recipes={recipes} ingredients={ingredients} onReload={loadData} onCalculate={calculate} />}
        {tab === 'nutrition' && <NutritionPage nutritionData={nutritionData} recipes={recipes} onCalculate={calculate} />}
      </main>
      <footer className="bg-white border-t mt-16">
        <div className="max-w-7xl mx-auto py-6 px-4 text-center text-sm text-gray-600">
          <strong>NutriApp</strong> © 2024 • ANVISA RDC nº 429/2020 • TBCA (USP)
        </div>
      </footer>
    </div>
  );
};

export default NutriApp;
