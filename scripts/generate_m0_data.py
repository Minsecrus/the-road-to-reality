"""Rebuild the M0 recipes, vanilla-texture models, worldgen and recipe-book entries."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'


def write(path, value):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def item(name):
    return {'item': name if ':' in name else 'trtr:' + name}


def recipe(name, pattern, key, output=None, components=None):
    result = {'id': output or 'trtr:' + name, 'count': 1}
    if components:
        result['components'] = components
    write(f'data/trtr/recipe/{name}.json', {
        'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern,
        'key': key, 'result': result})


blade, rope, stick = item('flint_blade'), item('rope'), item('minecraft:stick')
recipe('flint_axe', ['BB', 'RS'], {'B': blade, 'R': rope, 'S': stick})
recipe('flint_hoe', ['BS', 'RS'], {'B': blade, 'R': rope, 'S': stick})
recipe('flint_knife', ['B ', 'RS'], {'B': blade, 'R': rope, 'S': stick})
recipe('flint_shovel', ['BR', ' S'], {'B': blade, 'R': rope, 'S': stick})
recipe('crude_stone_pickaxe', ['CC', 'RS'], {'C': item('loose_stone'), 'R': rope, 'S': stick})
recipe('grass_bundle', ['FF', 'FF'], {'F': item('plant_fiber')})
recipe('fire_drill', ['SR', 'S '], {'R': rope, 'S': stick})
recipe('drying_rack', ['SS', 'RR'], {'R': rope, 'S': stick})
recipe('campfire', ['LG', 'LL'], {'L': {'tag': 'minecraft:logs_that_burn'}, 'G': item('grass_bundle')},
       'minecraft:campfire', {'minecraft:block_state': {'lit': 'false'}})
write('data/trtr/recipe/rope.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
      'ingredients': [item('plant_fiber')] * 3, 'result': {'id': 'trtr:rope', 'count': 1}})

# Change natural animal loot, never death inventories or leather carried by pack animals.
write('data/neoforge/loot_modifiers/global_loot_modifiers.json', {
    'replace': False, 'entries': ['trtr:raw_hides']})
write('data/trtr/loot_modifiers/raw_hides.json', {'type': 'trtr:raw_hides', 'conditions': [{
    'condition': 'minecraft:any_of', 'terms': [
        {'condition': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:entities/' + animal}
        for animal in ['cow', 'donkey', 'hoglin', 'horse', 'llama', 'mooshroom', 'mule', 'trader_llama']]}]})

# Reuse vanilla textures; no additional texture atlas or external asset dependency.
textures = {'flint_blade': 'trtr:item/flint_shard', 'rope': 'minecraft:item/lead',
            'hide': 'minecraft:item/leather', 'scraped_hide': 'minecraft:item/rabbit_hide',
            'grass_bundle': 'minecraft:item/wheat', 'fire_drill': 'minecraft:item/stick'}
for name, texture in textures.items():
    model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}}
    if name == 'fire_drill':
        model['textures']['layer1'] = 'minecraft:item/string'
    write(f'assets/trtr/models/item/{name}.json', model)
write('assets/trtr/models/block/soft_rock.json',
      {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'minecraft:block/tuff'}})
write('assets/trtr/blockstates/soft_rock.json', {'variants': {'': {'model': 'trtr:block/soft_rock'}}})
for block in ['soft_rock', 'drying_rack']:
    write(f'assets/trtr/models/item/{block}.json', {'parent': f'trtr:block/{block}'})


def box(start, end, texture):
    return {'from': start, 'to': end, 'faces': {
        face: {'texture': texture} for face in ['north', 'south', 'east', 'west', 'up', 'down']}}


rack = {'parent': 'minecraft:block/block', 'textures': {'particle': 'minecraft:block/oak_planks',
        'wood': 'minecraft:block/oak_log', 'hide': 'minecraft:block/brown_wool'},
        'elements': [box([1, 0, 6], [3, 16, 10], '#wood'), box([13, 0, 6], [15, 16, 10], '#wood'),
                     box([1, 13, 6], [15, 16, 10], '#wood')]}
write('assets/trtr/models/block/drying_rack.json', rack)
rack['elements'].append(box([3, 4, 7], [13, 13, 9], '#hide'))
write('assets/trtr/models/block/drying_rack_wet.json', rack)
rack['textures']['hide'] = 'minecraft:block/orange_terracotta'
write('assets/trtr/models/block/drying_rack_dry.json', rack)
write('assets/trtr/blockstates/drying_rack.json', {'variants': {
    'stage=0': {'model': 'trtr:block/drying_rack'},
    'stage=1': {'model': 'trtr:block/drying_rack_wet'},
    'stage=2': {'model': 'trtr:block/drying_rack_wet'},
    'stage=3': {'model': 'trtr:block/drying_rack_dry'}}})


def pool(name, conditions=None, count=1):
    entry = {'type': 'minecraft:item', 'name': name}
    if count != 1:
        entry['functions'] = [{'function': 'minecraft:set_count', 'count': count}]
    result = {'rolls': 1, 'entries': [entry]}
    if conditions:
        result['conditions'] = conditions
    return result


write('data/trtr/loot_table/blocks/soft_rock.json', {'type': 'minecraft:block', 'pools': [
    pool('trtr:loose_stone', count=2),
    pool('trtr:flint_shard', [{'condition': 'minecraft:random_chance', 'chance': 0.25}])]})
rack_pools = [pool('trtr:drying_rack')]
for stage in [1, 2, 3]:
    rack_pools.append(pool('minecraft:leather' if stage == 3 else 'trtr:scraped_hide', [{
        'condition': 'minecraft:block_state_property', 'block': 'trtr:drying_rack',
        'properties': {'stage': str(stage)}}]))
write('data/trtr/loot_table/blocks/drying_rack.json', {'type': 'minecraft:block', 'pools': rack_pools})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': ['trtr:soft_rock']})
write('data/minecraft/tags/block/mineable/axe.json', {'replace': False, 'values': ['trtr:drying_rack']})
write('data/trtr/worldgen/configured_feature/soft_rock.json', {'type': 'minecraft:ore', 'config': {
    'size': 24, 'discard_chance_on_air_exposure': 0.0, 'targets': [{
        'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:base_stone_overworld'},
        'state': {'Name': 'trtr:soft_rock'}}]}})
write('data/trtr/worldgen/placed_feature/soft_rock.json', {'feature': 'trtr:soft_rock', 'placement': [
    {'type': 'minecraft:count', 'count': 8}, {'type': 'minecraft:in_square'},
    {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
     'min_inclusive': {'absolute': 48}, 'max_inclusive': {'absolute': 128}}}, {'type': 'minecraft:biome'}]})
write('data/trtr/neoforge/biome_modifier/add_soft_rock.json', {'type': 'neoforge:add_features',
    'biomes': '#minecraft:is_overworld', 'features': 'trtr:soft_rock', 'step': 'underground_ores'})
write('data/trtr/worldgen/configured_feature/loose_stone_patch.json', {
    'type': 'minecraft:random_patch', 'config': {'tries': 16, 'xz_spread': 5, 'y_spread': 0, 'feature': {
        'feature': {'type': 'minecraft:simple_block', 'config': {'to_place': {
            'type': 'minecraft:simple_state_provider', 'state': {'Name': 'trtr:loose_stone'}}}},
        'placement': [{'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING_NO_LEAVES'}, {
            'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
                {'type': 'minecraft:matching_blocks', 'blocks': ['minecraft:air']},
                {'type': 'minecraft:would_survive', 'state': {'Name': 'trtr:loose_stone'}}]}}]}}})

unlock_items = {'rope': 'plant_fiber', 'grass_bundle': 'plant_fiber', 'fire_drill': 'rope',
                'drying_rack': 'scraped_hide', 'campfire': 'grass_bundle', 'crude_stone_pickaxe': 'loose_stone'}
for name in ['flint_axe', 'flint_hoe', 'flint_knife', 'flint_shovel']:
    unlock_items[name] = 'flint_blade'
for name, ingredient in unlock_items.items():
    write(f'data/trtr/advancement/recipes/{name}.json', {'parent': 'minecraft:recipes/root',
        'criteria': {'has_material': {'trigger': 'minecraft:inventory_changed', 'conditions': {
            'items': [{'items': 'trtr:' + ingredient}]}}}, 'requirements': [['has_material']],
        'rewards': {'recipes': ['trtr:' + name]}})

translations = {
 'item.trtr.flint_blade': ('燧石刃', 'Knapped Flint Blade'), 'item.trtr.rope': ('植物绳', 'Fiber Cord'),
 'item.trtr.hide': ('兽皮', 'Raw Hide'), 'item.trtr.scraped_hide': ('刮制皮', 'Scraped Hide'),
 'item.trtr.grass_bundle': ('干草束', 'Tinder Bundle'), 'item.trtr.fire_drill': ('钻木取火器', 'Fire Drill'),
 'block.trtr.soft_rock': ('风化软岩', 'Weathered Rock'), 'block.trtr.drying_rack': ('晾皮架', 'Hide Drying Rack'),
 'menu.trtr.knapping': ('燧石打制', 'Flint Knapping'), 'menu.trtr.new_flint': ('重新取材', 'New flint'),
 'menu.trtr.knapping_hint': ('保留描边的刃形，敲掉周围部分。', 'Keep the outlined blade. Chip away the surrounding flint.'),
 'menu.trtr.knapping_edge': ('从边缘开始剥片', 'Chip from an exposed edge'),
 'menu.trtr.knapping_ruined': ('刃部已破损，需要重新取材', 'Blade damaged. Start with new flint.'),
 'menu.trtr.knapping_done': ('燧石刃已收入背包', 'Blade added to inventory'),
 'message.trtr.knapping_no_flint': ('背包中没有燧石或燧石片', 'No flint or flint shards in your inventory'),
 'message.trtr.fire_needs_dry_campfire': ('需要未点燃且不淋雨、不浸水的篝火', 'Find an unlit campfire sheltered from rain and water'),
 'message.trtr.fire_needs_tinder': ('另一只手需要拿着干草束', 'Hold a tinder bundle in your other hand'),
 'message.trtr.fire_progress': ('钻木取火：%s%%', 'Starting fire: %s%%'),
 'message.trtr.fire_lit': ('篝火点燃了！木棍可在火上引燃为火把。', 'Fire lit! Use a stick on the fire to make a torch.'),
 'tooltip.trtr.flint': ('右键打开打制界面，剥片制作燧石刃', 'Use to open knapping and shape a flint blade'),
 'tooltip.trtr.fiber': ('三份合成植物绳；四份合成干草束。成熟小麦也产纤维。', 'Craft 3 into cord or 4 into tinder. Mature wheat also yields fiber.'),
 'tooltip.trtr.knife': ('右键原木刮木棍；另一只手持兽皮时，右键刮皮', 'Use on an unstripped log for sticks; use with raw hide in the other hand to scrape it'),
 'tooltip.trtr.fire_drill': ('另一只手拿干草束，对准未点燃的篝火，按住右键四秒', 'Hold tinder in the other hand; aim at an unlit campfire and hold Use for 4 seconds'),
 'tooltip.trtr.hide': ('另一只手持燧石刮刀，右键刮制', 'Use a flint knife in the other hand to scrape this hide'),
 'tooltip.trtr.drying': ('将刮制皮挂上晾皮架，避雨晾干三十秒后取下', 'Hang scraped hide on a rack; keep it out of rain for 30 seconds, then collect')}
for index, locale in enumerate(['zh_cn', 'en_us']):
    path = f'assets/trtr/lang/{locale}.json'
    content = json.loads((ROOT / path).read_text(encoding='utf-8'))
    content.update({key: values[index] for key, values in translations.items()})
    write(path, content)

print('M0 data generated.')
